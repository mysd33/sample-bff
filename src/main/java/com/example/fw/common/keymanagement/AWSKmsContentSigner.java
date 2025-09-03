package com.example.fw.common.keymanagement;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.keymanagement.config.KeyManagementConfigurationProperties;
import com.example.fw.common.message.CommonFrameworkMessageIds;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.MessageType;

/**
 * Bouncy CastleのContentSignerインターフェースを実装した AWS KMSを使用してコンテンツに署名するためのクラス<br>
 * 
 * CSR（証明書署名要求）や自己署名証明書を作成する際に、各コンテンツに署名するために使用される<br>
 */
public class AWSKmsContentSigner implements ContentSigner {
    private final KmsAsyncClient kmsAsyncClient;
    private final KeyInfo keyInfo;
    private final KeyManagementConfigurationProperties keyManagementConfigurationProperties;
    private final AlgorithmIdentifier algorithmIdentifier;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    /**
     * コンストラクタ
     * 
     * @param kmsAsyncClient                       AWS KMSの非同期クライアント
     * @param keyInfo                              署名に使用するキー情報
     * @param keyManagementConfigurationProperties キー管理の設定プロパティ
     */
    public AWSKmsContentSigner(final KmsAsyncClient kmsAsyncClient, final KeyInfo keyInfo,
            KeyManagementConfigurationProperties keyManagementConfigurationProperties) {
        this.kmsAsyncClient = kmsAsyncClient;
        this.keyInfo = keyInfo;
        this.keyManagementConfigurationProperties = keyManagementConfigurationProperties;
        this.algorithmIdentifier = new DefaultSignatureAlgorithmIdentifierFinder()
                .find(keyManagementConfigurationProperties.getSignatureAlgorithm());
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public byte[] getSignature() {
        byte[] dataToSign = outputStream.toByteArray();
        String messageDigestAlgorithm = keyManagementConfigurationProperties.getHashAlgorithm();
        byte[] hash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
            hash = messageDigest.digest(dataToSign);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_KYMG_9007, messageDigestAlgorithm);
        }
        return kmsAsyncClient.sign(builder -> builder//
                .keyId(keyInfo.getKeyId()) // キーIDを指定
                .message(SdkBytes.fromByteArray(hash)) // ハッシュ値をメッセージとして指定
                .messageType(MessageType.DIGEST) // 署名のメッセージタイプをDIGESTに設定
                .signingAlgorithm(keyManagementConfigurationProperties.getAwsKms().getKmsSigningAlgorithmSpec())) // 署名アルゴリズムを指定
                .thenApply(response -> {
                    // 署名の結果を取得
                    return response.signature().asByteArray();
                }).join(); // 非同期処理の完了を待つ

    }

}
