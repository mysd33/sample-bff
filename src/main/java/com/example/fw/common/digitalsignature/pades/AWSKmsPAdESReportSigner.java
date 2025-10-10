package com.example.fw.common.digitalsignature.pades;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.example.fw.common.digitalsignature.ReportSigner;
import com.example.fw.common.digitalsignature.SignOptions;
import com.example.fw.common.digitalsignature.config.DigitalSignatureConfigurationProperties;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.keymanagement.Certificate;
import com.example.fw.common.keymanagement.KeyInfo;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.reports.DefaultReport;
import com.example.fw.common.reports.Report;
import com.example.fw.common.reports.ReportsConstants;
import com.example.fw.common.reports.config.ReportsConfigurationProperties;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
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
    private final ReportsConfigurationProperties reportsConfigurationProperties;
    private final DigitalSignatureConfigurationProperties digitalSignatureConfigurationProperties;

    // PDFの一時保存ファイルのディレクトリ（パスを初期化設定後、定期削除のための別スレッドで参照されるためAtomicReferenceにしておく）
    private final AtomicReference<Path> pdfTempPath = new AtomicReference<>();
    // 電子署名に使用するキーID
    private String keyId;
    // 電子署名に使用する証明書
    private Certificate certificate;

    @PostConstruct
    public void init() {
        // 帳票を一時保存する一時ディレクトリを作成する
        pdfTempPath.set(Path.of(ReportsConstants.TMP_DIR, reportsConfigurationProperties.getReportTmpdir()));
        appLogger.debug("pdfTempPath: {}", pdfTempPath);
        // 一時ディレクトリが存在しない場合は作成する
        pdfTempPath.get().toFile().mkdirs();

        // キーIDの取得
        keyId = digitalSignatureConfigurationProperties.getAwsKms().getKeyId();
        if (StringUtils.isEmpty(keyId)) {
            // キーIDが指定されていない場合、キーエイリアスからキーIDを取得
            String keyAlias = digitalSignatureConfigurationProperties.getAwsKms().getKeyAlias();
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
        // 証明書の取得
        certificate = keyManager.getCertificateFromObjectStorage(KeyInfo.builder().keyId(keyId).build());
    }

    @Override
    public Report sign(Report originalReport) {
        return sign(originalReport, SignOptions.builder().build());
    }

    @Override
    public Report sign(Report originalReport, SignOptions options) {
        DSSDocument toSignDocument = null;
        if (originalReport instanceof DefaultReport defaultReport) {
            // Fileに対して電子署名付与を実装
            toSignDocument = new FileDocument(defaultReport.getFile());
        } else {
            // InMemoryDocumentに対して電子署名付与を実装
            toSignDocument = new InMemoryDocument(originalReport.getInputStream());
        }

        try (AWSKmsSignatureToken token = new AWSKmsSignatureToken(keyManager, keyId)) {

            // TODO: 可視署名は現在未対応
            if (options.isVisible()) {
                appLogger.debug("可視署名は現在未対応");
            }

            // 証明書をX.509証明書形式で取得
            X509Certificate x509Certificate;
            try {
                x509Certificate = certificate.getX509Certificate();
            } catch (CertificateException | IOException e) {
                throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9008);
            }

            // 証明書の有効性を確認
            validateCertificate(x509Certificate);

            // PAdESSignatureの署名パラメータを作成
            CertificateToken certificateToken = new CertificateToken(x509Certificate);
            PAdESSignatureParameters signatureParameters = createSignatureParameters(certificateToken, options);

            // 証明書検証機能を初期化
            CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
            certificateVerifier.setCheckRevocationForUntrustedChains(false);

            // PAdES署名サービスを作成
            PAdESService padesService = new PAdESService(certificateVerifier);

            // 署名対象のハッシュ値を計算
            ToBeSigned dataToSign = padesService.getDataToSign(toSignDocument, signatureParameters);

            // 署名を生成
            SignatureValue signatureValue = token.sign(dataToSign, certificateToken.getSignatureAlgorithm(), null);

            // 署名をPDFに適用
            DSSDocument signedDocument = padesService.signDocument(toSignDocument, //
                    signatureParameters, signatureValue);

            // 署名済みPDFを一時ファイルに保存しReportオブジェクトを生成して返却
            Path tempFielPath;
            try {
                tempFielPath = Files.createTempFile(pdfTempPath.get(), ReportsConstants.PDF_TEMP_FILE_PREFIX,
                        ReportsConstants.PDF_FILE_EXTENSION);
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(tempFielPath))) {
                    signedDocument.writeTo(bos);
                }
                File file = tempFielPath.toFile();
                return DefaultReport.builder().file(file).build();
            } catch (IOException e) {
                throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9002);
            }
        }
    }

    /**
     * 証明書の有効性を検証する
     * 
     * @param certificate 検証対象の証明書
     */
    private void validateCertificate(X509Certificate certificate) {
        Assert.notNull(certificate, "Certificate must not be null");
        try {
            // 証明書の有効期限を確認
            certificate.checkValidity();
        } catch (CertificateNotYetValidException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9004);
        } catch (CertificateExpiredException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9005);
        }
    }

    /**
     * PAdESSignatureParametersを作成する
     * 
     * @param privateKey 署名に使用する秘密鍵
     * @return PAdESSignatureParameters
     */
    private PAdESSignatureParameters createSignatureParameters(CertificateToken certificateToken, SignOptions options) {
        PAdESSignatureParameters pAdESSignatureParameters = new PAdESSignatureParameters();
        // 証明書の設定
        // 署名に使用する証明書を設定し、公開鍵情報から暗号化アルゴリズムを取得し設定
        pAdESSignatureParameters.setSigningCertificate(certificateToken);
        // TODO: 証明書チェーンで中間証明書が必要な場合は修正が必要
        pAdESSignatureParameters.setCertificateChain(certificateToken);

        // 署名の設定
        // 署名レベルをPAdES_BASELINE Bプロファイルに設定
        pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        // 署名のパッケージング形式をENVELOPED（署名をPDF文書に埋め込む）に設定
        pAdESSignatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        // 証明書の内容からハッシュアルゴリズムの設定
        pAdESSignatureParameters.setDigestAlgorithm(certificateToken.getSignatureAlgorithm().getDigestAlgorithm());

        // 署名の理由、場所を設定
        pAdESSignatureParameters.setReason(options.getReason());
        pAdESSignatureParameters.setLocation(options.getLocation());

        // TODO: 可視署名は現在未対応
        return pAdESSignatureParameters;
    }

}
