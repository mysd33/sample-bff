package com.example.fw.common.digitalsignature.pades;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import com.example.fw.common.digitalsignature.ReportSigner;
import com.example.fw.common.digitalsignature.SignOptions;
import com.example.fw.common.digitalsignature.config.DigitalSignatureConfigurationProperties;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.file.TempFileCreator;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.reports.DefaultReport;
import com.example.fw.common.reports.Report;
import com.example.fw.common.reports.ReportsConstants;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PKCS#12形式のキーストアを使用してPDFにPAdES形式の電子署名を付与するクラス
 * 
 * このクラスは、PKCS#12形式のキーストアから秘密鍵と証明書を読み込み、PDFにPAdES形式の電子署名を付与します。
 * 
 * @see ReportSigner
 */
@Slf4j
@RequiredArgsConstructor
public class PKCS12PAdESReportSiginer implements ReportSigner {
    private final TempFileCreator tempFileCreator;
    private final DigitalSignatureConfigurationProperties digitalSignatureConfigurationProperties;

    @Override
    public Report sign(final Report originalReport) {
        return sign(originalReport, SignOptions.builder().build());
    }

    @Override
    public Report sign(final Report originalReport, final SignOptions options) {
        DSSDocument toSignDocument = null;
        if (originalReport instanceof DefaultReport defultReport) {
            // Fileに対して電子署名付与を実装
            toSignDocument = new FileDocument(defultReport.getFile());
        } else {
            toSignDocument = new InMemoryDocument(originalReport.getInputStream());
        }

        DSSPrivateKeyEntry privateKey = null;
        try (Pkcs12SignatureToken pkcs12SignatureToken = new Pkcs12SignatureToken(
                digitalSignatureConfigurationProperties.getPkcs12().getKeystoreFilePath(), //
                new PasswordProtection(
                        digitalSignatureConfigurationProperties.getPkcs12().getPassword().toCharArray()))) {
            privateKey = pkcs12SignatureToken.getKeys().get(0);

            // 証明書の有効性を確認
            X509Certificate certificate = privateKey.getCertificate().getCertificate();
            CertificateUtils.validateCertificate(certificate);

            // PAdESSignatureの署名パラメータを作成
            PAdESSignatureParameters signatureParameters = createSignatureParameters(privateKey, options);

            // 証明書検証機能を初期化
            CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
            // PAdES署名サービスを作成
            PAdESService padesService = new PAdESService(certificateVerifier);
            // 署名対象のハッシュ値を計算
            ToBeSigned dataToSign = padesService.getDataToSign(toSignDocument, signatureParameters);
            // 計算されたハッシュ値を使用して署名を生成
            SignatureValue signatureValue = pkcs12SignatureToken.sign(dataToSign,
                    signatureParameters.getDigestAlgorithm(), privateKey);
            // 署名をPDFに適用
            DSSDocument signedDocument = padesService.signDocument(toSignDocument, //
                    signatureParameters, signatureValue);
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

    /**
     * PAdESSignatureParametersを作成する
     * 
     * @param privateKey 署名に使用する秘密鍵
     * @return PAdESSignatureParameters
     */
    private PAdESSignatureParameters createSignatureParameters(final DSSPrivateKeyEntry privateKey,
            final SignOptions options) {
        PAdESSignatureParameters pAdESSignatureParameters = new PAdESSignatureParameters();
        // 証明書の設定
        // 署名に使用する証明書を設定し、公開鍵情報から暗号化アルゴリズムを取得し設定
        pAdESSignatureParameters.setSigningCertificate(privateKey.getCertificate());
        pAdESSignatureParameters.setCertificateChain(privateKey.getCertificateChain());

        // 署名の設定
        // 署名レベルをPAdES_BASELINE Bプロファイルに設定
        pAdESSignatureParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        // 署名のパッケージング形式をENVELOPED（署名をPDF文書に埋め込む）に設定
        pAdESSignatureParameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        // 署名に使用するハッシュアルゴリズムの設定
        pAdESSignatureParameters.setDigestAlgorithm(
                DigestAlgorithm.valueOf(digitalSignatureConfigurationProperties.getPkcs12().getHashAlgorithm()));

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
