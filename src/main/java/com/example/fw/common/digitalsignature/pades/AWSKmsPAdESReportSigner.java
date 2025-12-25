package com.example.fw.common.digitalsignature.pades;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.example.fw.common.digitalsignature.ReportSigner;
import com.example.fw.common.digitalsignature.SignOptions;
import com.example.fw.common.digitalsignature.config.DigitalSignatureConfigurationProperties;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.file.TempFileCreator;
import com.example.fw.common.keymanagement.Certificate;
import com.example.fw.common.keymanagement.KeyInfo;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.keymanagement.config.KeyManagementConfigurationProperties;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.reports.DefaultReport;
import com.example.fw.common.reports.Report;
import com.example.fw.common.reports.ReportsConstants;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AWS KMSを使用してPDFにPAdES形式の電子署名を付与するクラス
 * 
 * このクラスは、AWS KMSを使用してPDFにPAdES形式の電子署名を付与します。
 * 
 * @see ReportSigner
 */
@Slf4j
@RequiredArgsConstructor
public class AWSKmsPAdESReportSigner implements ReportSigner {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final KeyManager keyManager;
    private final TempFileCreator tempFileCreator;
    private final DigitalSignatureConfigurationProperties digitalSignatureConfigurationProperties;
    private final KeyManagementConfigurationProperties keyManagementConfigurationProperties;

    // 電子署名に使用するキーID
    private String keyId;
    // 電子署名に使用する証明書チェーンのCertificateToken
    private List<CertificateToken> certificateTokens;

    @PostConstruct
    public void init() {

        // キーIDの取得
        String keyAlias = null;
        // キーIDが明示的に設定されている場合はそのまま使用
        keyId = digitalSignatureConfigurationProperties.getAwsKms().getKeyId();
        if (StringUtils.isEmpty(keyId)) {
            // キーIDが設定されていない場合、キーエイリアスからキーIDを取得
            keyAlias = digitalSignatureConfigurationProperties.getAwsKms().getKeyAlias();
            if (StringUtils.isEmpty(keyAlias)) {
                throw new IllegalStateException(
                        "Either keyId or keyAlias must be specified in digital signature configuration.");
            }
            // キーエイリアスからキーIDを取得
            keyId = keyManager.findKeyByAlias(keyAlias).getKeyId();
            if (StringUtils.isEmpty(keyId)) {
                throw new IllegalStateException(
                        String.format("No key found for the specified key alias: %s", keyAlias));
            }
        }
        // 解析用にキーIDをログ出力
        appLogger.info(CommonFrameworkMessageIds.I_FW_PDFSGN_0001, keyAlias, keyId);

        // 証明書の取得
        List<Certificate> certificates = keyManager
                .getCertificatesFromObjectStorage(KeyInfo.builder().keyId(keyId).build());
        // 証明書が取得できない場合は例外をスロー
        if (certificates == null) {
            throw new IllegalStateException(String.format("No certificate found for the specified key ID: %s", keyId));
        }
        // 証明書チェーンをCertificateToken形式で取得
        certificateTokens = CertificateUtils.exchageOrderdCertifcateTokens(certificates);

        // KMSから公開鍵を取得
        PublicKey publicKey = keyManager.getPublicKey(KeyInfo.builder().keyId(keyId).build());
        // エンドエンティティ証明書の公開鍵を取得
        PublicKey publicKeyInCertificate = certificateTokens.getFirst().getPublicKey();
        // KMSから取得した公開鍵と証明書の公開鍵の内容が一致するか確認
        if (!publicKey.equals(publicKeyInCertificate)) {
            throw new IllegalStateException(
                    "The public key from KMS does not match the public key in the certificate.");
        }
    }

    @Override
    public Report sign(final Report originalReport) {
        return sign(originalReport, SignOptions.builder().build());
    }

    @Override
    public Report sign(final Report originalReport, final SignOptions options) {
        DSSDocument toSignDocument = null;
        if (originalReport instanceof DefaultReport defaultReport) {
            // Fileに対して電子署名付与を実装
            toSignDocument = new FileDocument(defaultReport.getFile());
        } else {
            // InMemoryDocumentに対して電子署名付与を実装
            toSignDocument = new InMemoryDocument(originalReport.getInputStream());
        }

        try (AWSKmsSignatureToken token = new AWSKmsSignatureToken(keyManager, keyId)) {

            // PAdESSignatureの署名パラメータを作成
            PAdESSignatureParameters signatureParameters = createSignatureParameters(certificateTokens, options);

            // 証明書検証機能を初期化
            CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
            certificateVerifier.setCheckRevocationForUntrustedChains(false);

            // PAdES署名サービスを作成
            PAdESService padesService = new PAdESService(certificateVerifier);

            // 署名対象のハッシュ値を計算
            ToBeSigned dataToSign = padesService.getDataToSign(toSignDocument, signatureParameters);

            // 署名を生成
            SignatureValue signatureValue = token.sign(dataToSign, signatureParameters.getSignatureAlgorithm(), null);

            // 署名をPDFに適用
            DSSDocument signedDocument = padesService.signDocument(toSignDocument, //
                    signatureParameters, signatureValue);

            // 署名済みPDFを一時ファイルに保存しReportオブジェクトを生成して返却

            try {
                File tempFile = tempFileCreator.createTempFile(ReportsConstants.PDF_TEMP_FILE_PREFIX,
                        ReportsConstants.PDF_FILE_EXTENSION);
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    signedDocument.writeTo(bos);
                }
                return DefaultReport.builder().file(tempFile).build();
            } catch (IOException e) {
                throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9002);
            }
        }
    }

    /**
     * PAdESSignatureParametersを作成する
     * 
     * @param privateKey 署名に使用する秘密鍵
     * @return PAdESSignatureParameters
     */
    private PAdESSignatureParameters createSignatureParameters(final List<CertificateToken> certificateTokens,
            final SignOptions options) {
        PAdESSignatureParameters pAdESSignatureParameters = new PAdESSignatureParameters();
        // 証明書の設定
        // 署名に使用する証明書を設定し、公開鍵情報から暗号化アルゴリズムを取得し設定
        pAdESSignatureParameters.setSigningCertificate(certificateTokens.getFirst());
        // 証明書チェーンの設定
        pAdESSignatureParameters.setCertificateChain(certificateTokens);

        // 署名の設定
        // 署名レベルをPAdES_BASELINE Bプロファイルに設定
        pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        // 署名のパッケージング形式をENVELOPED（署名をPDF文書に埋め込む）に設定
        pAdESSignatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        // 署名に使用するハッシュアルゴリズムの設定
        pAdESSignatureParameters
                .setDigestAlgorithm(DigestAlgorithm.forName(keyManagementConfigurationProperties.getHashAlgorithm()));

        // 署名に使用する暗号化アルゴリズムを設定
        // 例えば公開鍵の暗号化アルゴリズムから取得するとrsaEncryption（RSA）となるが、rsassaPss(RSASSA-PSS)にするには、明示的に設定が必要。
        pAdESSignatureParameters.setEncryptionAlgorithm(
                EncryptionAlgorithm.forName(keyManagementConfigurationProperties.getKeyFactoryAlgorithm()));

        // アプリケーション名、署名の理由、場所を設定
        pAdESSignatureParameters.setAppName(digitalSignatureConfigurationProperties.getApplicationName());
        pAdESSignatureParameters.setReason(options.getReason());
        pAdESSignatureParameters.setLocation(options.getLocation());

        // パスワード保護されたPDFの場合のパスワード設定
        if (StringUtils.isNotEmpty(options.getUserPassword())) {
            pAdESSignatureParameters.setPasswordProtection(options.getUserPassword().toCharArray());
        }

        // 可視署名
        // https://github.com/esig/dss/blob/master/dss-cookbook/src/test/java/eu/europa/esig/dss/cookbook/example/sign/SignPdfPadesBVisibleTest.java
        if (options.isVisible()) {
            SignatureImageParameters imageParameters = new SignatureImageParameters();
            imageParameters.setImage(new FileDocument(options.getVisibleSignImagePath()));
            SignatureFieldParameters fieldParameters = new SignatureFieldParameters();
            imageParameters.setFieldParameters(fieldParameters);
            fieldParameters.setPage(options.getVisibleSignPage());
            fieldParameters.setOriginX(options.getVisibleSignRect()[0]);
            fieldParameters.setOriginY(options.getVisibleSignRect()[1]);
            fieldParameters.setWidth(options.getVisibleSignRect()[2] - options.getVisibleSignRect()[0]);
            fieldParameters.setHeight(options.getVisibleSignRect()[3] - options.getVisibleSignRect()[1]);
            SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
            textParameters.setText(options.getVisibleSignText());
            pAdESSignatureParameters.setImageParameters(imageParameters);
        }

        return pAdESSignatureParameters;
    }

}
