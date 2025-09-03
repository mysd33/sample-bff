package com.example.fw.common.keymanagement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.keymanagement.config.KeyManagementConfigurationProperties;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.objectstorage.DownloadObject;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.UploadObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;

/**
 * AWSのKMSを使ってキー管理を行うKeyManager実装クラス
 * 
 */
@Slf4j
@RequiredArgsConstructor
public class AWSKmsKeyManager implements KeyManager {
    private static final String KEY_ALIAS_PREFIX = "alias/"; // キーエイリアスはalias/で始まる必要がある
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final KmsAsyncClient kmsAsyncClient;
    private final ObjectStorageFileAccessor objectStorageFileAccessor;
    private final KeyManagementConfigurationProperties keyManagementConfigurationProperties;

    static {
        // BouncyCastleプロバイダを登録
        Security.addProvider(new BouncyCastleProvider());
    }

    // 参考：AWS SDK for JavaのKMSのサンプルコード例
    // https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/java_kms_code_examples.html
    // https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/example_code/kms#code-examples

    @Override
    public KeyInfo createKey(String keyAlias) {
        // KMSを使って暗号鍵を生成する
        KeyInfo keyInfo = kmsAsyncClient.createKey(builder -> builder//
                .keySpec(keyManagementConfigurationProperties.getAwsKms().getKeySpec())//
                .keyUsage(keyManagementConfigurationProperties.getAwsKms().getKeyUsage())
                .description(keyManagementConfigurationProperties.getAwsKms().getKeyDescription()))//
                .thenApply(response -> //
                KeyInfo.builder().//
                        keyId(response.keyMetadata().keyId()) // レスポンスからキーIDを取得
                        .state(response.keyMetadata().keyStateAsString()) // レスポンスからキーの状態を取得
                        .build())
                .join();
        // キーのエイリアスを作成
        kmsAsyncClient.createAlias(builder -> builder//
                .aliasName(KEY_ALIAS_PREFIX + keyAlias) // エイリアス名を指定
                .targetKeyId(keyInfo.getKeyId()) // 作成したキーIDを指定
        ).join();

        return keyInfo;
    }

    @Override
    public KeyInfo deleteKey(final KeyInfo keyInfo) {
        final String keyId = keyInfo.getKeyId();
        final int pendingWindowInDays = keyManagementConfigurationProperties.getAwsKms().getPendingDeleteWindowInDays();
        // KMSを使って暗号鍵を削除する
        return kmsAsyncClient.scheduleKeyDeletion(builder -> builder//
                .keyId(keyId).pendingWindowInDays(pendingWindowInDays))//
                .thenApply(response -> {
                    appLogger.debug("キー{}は{}後に削除します", keyId, pendingWindowInDays);
                    return KeyInfo.builder()//
                            .keyId(keyId) // レスポンスからキーIDを取得
                            .state(response.keyStateAsString()) // レスポンスからキーの状態を取得
                            .build();
                }).join();
    }

    @Override
    public PublicKey getPublicKey(final KeyInfo keyInfo) {
        // KMSから公開鍵を取得する
        return kmsAsyncClient.getPublicKey(builder -> builder//
                .keyId(keyInfo.getKeyId()))//
                .thenApply(response -> {
                    byte[] publicKeyBytes = response.publicKey().asByteArray();
                    String algorithm = keyManagementConfigurationProperties.getKeyFactoryAlgorithm();
                    KeyFactory keyFactory;
                    try {
                        keyFactory = KeyFactory.getInstance(algorithm);
                        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                    } catch (NoSuchAlgorithmException e) {
                        throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9001, algorithm);
                    } catch (InvalidKeySpecException e) {
                        throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9002);
                    }
                }).join();
    }

    @Override
    public CertificateSigningRequest createCsr(final KeyInfo keyInfo, final String subject) {
        // KMSから公開鍵を取得
        PublicKey publicKey = getPublicKey(keyInfo);
        // サブジェクト情報を設定してCSRを作成
        X500Name subjectName = new X500Name(subject);
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(subjectName, publicKey);
        // KMSを使って電子署名したCSRを生成
        PKCS10CertificationRequest csr = csrBuilder
                .build(new AWSKmsContentSigner(kmsAsyncClient, keyInfo, keyManagementConfigurationProperties));
        try {
            return CertificateSigningRequest.builder() //
                    .der(csr.getEncoded()) // CSRのDERエンコードされたバイト配列を設定
                    .build();
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9003);
        }
    }

    @Override
    public Certificate createSelfSignedCertificate(final CertificateSigningRequest csr, final KeyInfo keyInfo) {
        // CSRの読み込み
        PKCS10CertificationRequest pkcs10Csr;
        try {
            pkcs10Csr = new PKCS10CertificationRequest(csr.getDer());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9004);
        }
        // CSRからSubjectと公開鍵を抽出
        X500Name subject = pkcs10Csr.getSubject();
        String algorithm = keyManagementConfigurationProperties.getKeyFactoryAlgorithm();
        PublicKey publicKey;
        try {
            byte[] pubKeyBytes = pkcs10Csr.getSubjectPublicKeyInfo().getEncoded();
            publicKey = KeyFactory.getInstance(algorithm)//
                    .generatePublic(new X509EncodedKeySpec(pubKeyBytes));
        } catch (IOException | InvalidKeySpecException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9005);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9001, algorithm);
        }
        // 自己署名の場合、IssuerはSubjectと同じ
        X500Name issuer = subject;
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        Instant now = Instant.now();
        // 署名の有効期限を設定
        Date notBefore = Date.from(now);
        Date notAfter = Date
                .from(now.plus(keyManagementConfigurationProperties.getSelfSignedCertValidityDays(), ChronoUnit.DAYS));
        // 証明書を作成
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(issuer, serialNumber, notBefore,
                notAfter, subject, publicKey);

        try {
            // Subject Key Identifierを取得
            /*
             * JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
             * certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false,
             * extensionUtils.createSubjectKeyIdentifier(pkcs10Csr.getSubjectPublicKeyInfo()
             * ));
             */
            X509CertificateHolder certificateHolder = certificateBuilder
                    .build(new AWSKmsContentSigner(kmsAsyncClient, keyInfo, keyManagementConfigurationProperties));
            return Certificate.builder() //
                    .der(certificateHolder.getEncoded()) // 証明書のDERエンコードされたバイト配列を設定
                    .build();
            // } catch (NoSuchAlgorithmException | IOException e) {
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9006);
        }
    }

    @Override
    public void saveCsrToObjectStorage(final CertificateSigningRequest csr, final KeyInfo keyInfo) { //
        final String certsBassPrefix = keyManagementConfigurationProperties.getCertsBasePrefix();
        final String certsCsrFileName = keyManagementConfigurationProperties.getCsrPemFileName();
        final String csrPrefix = certsBassPrefix + keyInfo.getKeyId() + "/" + certsCsrFileName;
        // PEM形式でCSRをエクスポート
        StringWriter csrWriter = new StringWriter();
        try {
            csr.exportPemTo(csrWriter);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9007);
        }
        appLogger.debug("CSR PEM: {}", csrWriter.toString());
        InputStream csrInputStream = new ByteArrayInputStream(csrWriter.toString().getBytes());
        // CSRのpemをS3にアップロード
        UploadObject csrUploadObject = UploadObject.builder().inputStream(csrInputStream).prefix(csrPrefix)
                .size(csrWriter.toString().getBytes().length).build();
        objectStorageFileAccessor.upload(csrUploadObject);
        appLogger.debug("CSRファイルをアップロード: {}", csrPrefix);
    }

    @Override
    public CertificateSigningRequest getCsrFromObjectStorage(final KeyInfo keyInfo) {
        final String certsBassPrefix = keyManagementConfigurationProperties.getCertsBasePrefix();
        final String certsCsrFileName = keyManagementConfigurationProperties.getCsrPemFileName();
        final String csrPrefix = certsBassPrefix + keyInfo.getKeyId() + "/" + certsCsrFileName;
        // オブジェクトストレージからCSRのpemをダウンロード
        DownloadObject downloadObject = objectStorageFileAccessor.download(csrPrefix);
        try {
            return CertificateSigningRequest.builder()//
                    .der(downloadObject.getInputStream().readAllBytes()).build();
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9008);
        }
    }

    @Override
    public void saveSelfSignedCertificateToObjectStorage(final Certificate certificate, final KeyInfo keyInfo) {
        final String certsBassPrefix = keyManagementConfigurationProperties.getCertsBasePrefix();
        final String selfSignedCertificateFileName = keyManagementConfigurationProperties
                .getSelfSignedCertPemFileName();
        final String certifacatePrefix = certsBassPrefix + keyInfo.getKeyId() + "/" + selfSignedCertificateFileName;
        // PEM形式で自己署名証明書をエクスポート
        StringWriter certWriter = new StringWriter();
        try {
            certificate.exportPemTo(certWriter);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9009);
        }
        appLogger.debug("自己署名証明書 PEM: {}", certWriter.toString());
        InputStream certInputStream = new ByteArrayInputStream(certWriter.toString().getBytes());
        // 自己署名証明書のpemをS3にアップロード
        UploadObject certUploadObject = UploadObject.builder().inputStream(certInputStream).prefix(certifacatePrefix)
                .size(certWriter.toString().getBytes().length).build();
        objectStorageFileAccessor.upload(certUploadObject);
        appLogger.debug("自己署名証明書ファイルをアップロード: {}", certifacatePrefix);
    }

    @Override
    public Certificate getSelfSignedCertificateFromObjectStorage(final KeyInfo keyInfo) {
        String selfSignedCertificateFileName = keyManagementConfigurationProperties.getSelfSignedCertPemFileName();
        try {
            return getCertificateFromObjectStorage(keyInfo, selfSignedCertificateFileName);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9010);
        }
    }

    @Override
    public Certificate getCertificateFromObjectStorage(final KeyInfo keyInfo) {
        String certificateFileName = keyManagementConfigurationProperties.getCertPemFileName();
        try {
            return getCertificateFromObjectStorage(keyInfo, certificateFileName);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9011);
        }
    }

    @Override
    public Signature createSignatureFromDigest(final byte[] digestData, final KeyInfo keyInfo) {
        SignRequest signRequest = SignRequest.builder()//
                .keyId(keyInfo.getKeyId())//
                .message(SdkBytes.fromByteArray(digestData)) // データをバイト配列として設定
                .signingAlgorithm(keyManagementConfigurationProperties.getAwsKms().getKmsSigningAlgorithmSpec())//
                .messageType(MessageType.DIGEST) // メッセージタイプをDIGESTに設定
                .build();
        return kmsAsyncClient.sign(signRequest)//
                .thenApply(response -> Signature.builder()//
                        .value(response.signature().asByteArray()).build())//
                .join();
    }

    @Override
    public Signature createSignatureFromRawData(byte[] rawData, KeyInfo keyInfo) {
        SignRequest signRequest = SignRequest.builder()//
                .keyId(keyInfo.getKeyId())//
                .message(SdkBytes.fromByteArray(rawData)) // データをバイト配列として設定
                .signingAlgorithm(keyManagementConfigurationProperties.getAwsKms().getKmsSigningAlgorithmSpec())//
                .messageType(MessageType.RAW) // メッセージタイプをRAWに設定
                .build();
        return kmsAsyncClient.sign(signRequest)//
                .thenApply(response -> Signature.builder()//
                        .value(response.signature().asByteArray()).build())//
                .join();
    }

    /**
     * オブジェクトストレージから証明書を取得するメソッド
     * 
     * @param keyInfo
     * @param certificateFileName
     * @return
     * @throws IOException
     */
    private Certificate getCertificateFromObjectStorage(final KeyInfo keyInfo, final String certificateFileName)
            throws IOException {
        final String certsBassPrefix = keyManagementConfigurationProperties.getCertsBasePrefix();
        final String certifacatePrefix = certsBassPrefix + keyInfo.getKeyId() + "/" + certificateFileName;
        // オブジェクトストレージから自己署名証明書のpemをダウンロード
        DownloadObject downloadObject = objectStorageFileAccessor.download(certifacatePrefix);

        return Certificate.builder()//
                .der(downloadObject.getInputStream().readAllBytes()) // 証明書のDERエンコードされたバイト配列を設定
                .build();

    }

}
