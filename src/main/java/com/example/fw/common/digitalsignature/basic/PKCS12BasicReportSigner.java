package com.example.fw.common.digitalsignature.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

/**
 * PKCS#12形式のキーストアを使用してPDFに基本的な電子署名を付与するクラス
 * 
 * このクラスは、PKCS#12形式のキーストアから秘密鍵と証明書を読み込み、PDFに電子署名を付与します。
 * 
 * @see ReportSigner
 */
@RequiredArgsConstructor
public class PKCS12BasicReportSigner implements ReportSigner {
    private static final String PKCS12 = "pkcs12";
    private static final int Y_AXIS_MAX_VALUE = 840; // A4縦サイズのY軸最大値
    private final TempFileCreator tempFileCreator;
    private final DigitalSignatureConfigurationProperties digitalSignatureConfig;

    @Override
    public Report sign(final Report originalReport) {
        return sign(originalReport, SignOptions.builder().build());
    }

    @Override
    public Report sign(final Report originalReport, final SignOptions options) {
        // デフォルトの署名処理としてOpenPDFを使用して、PDFに電子署名を付与する実装例
        // https://javadoc.io/doc/com.github.librepdf/openpdf/1.3.43/com/lowagie/text/pdf/PdfStamper.html#createSignature-com.lowagie.text.pdf.PdfReader-java.io.OutputStream-char-
        PdfReader originalPdfReader = null;
        try {
            // PDFがパスワード保護されている場合はパスワードを指定してPdfReaderを作成する
            if (StringUtils.isNotEmpty(options.getUserPassword())) {
                originalPdfReader = new PdfReader(originalReport.getInputStream(),
                        options.getUserPassword().getBytes());
            } else {
                originalPdfReader = new PdfReader(originalReport.getInputStream());
            }
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9001);
        }
        // メモリを極力使わないよう、PDFのファイルサイズが大きい場合も考慮し一時ファイルに出力してInputStreamで返却するようにする
        File signedPdfTempFilePath = tempFileCreator.createTempFile(ReportsConstants.PDF_TEMP_FILE_PREFIX,
                ReportsConstants.PDF_FILE_EXTENSION);
        try (FileOutputStream fos = new FileOutputStream(signedPdfTempFilePath)) {
            // PKCS#12形式のキーストアから秘密鍵と証明書を読み込む
            KeyStore ks = KeyStore.getInstance(PKCS12);
            ks.load(new FileInputStream(digitalSignatureConfig.getPkcs12().getKeystoreFilePath()),
                    digitalSignatureConfig.getPkcs12().getPassword().toCharArray());
            String alias = ks.aliases().nextElement();
            PrivateKey key = (PrivateKey) ks.getKey(alias,
                    digitalSignatureConfig.getPkcs12().getPassword().toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);
            // PDFに電子署名を付与する
            doSign(originalPdfReader, fos, key, chain, options);
            // 署名付きPDFの帳票を返却する
            return DefaultReport.builder()//
                    .file(signedPdfTempFilePath).build();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9007);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9002);
        } finally {
            originalPdfReader.close();
        }
    }

    /**
     * PDFに電子署名を付与する
     * 
     * @param originalPdfReader 元のPDFのPdfReader
     * @param fos               署名付きPDFの出力先のFileOutputStream
     * @param key               秘密鍵
     * @param chain             証明書チェーン
     * @param options           署名オプション
     */
    private void doSign(final PdfReader originalPdfReader, final FileOutputStream fos, final PrivateKey key,
            final Certificate[] chain, final SignOptions options) {
        try (PdfStamperWrapper pdfStamperWrapper = new PdfStamperWrapper(
                PdfStamper.createSignature(originalPdfReader, fos, '\0'))) {
            PdfSignatureAppearance sap = pdfStamperWrapper.getSignatureAppearance();
            // TODO: 署名したアプリケーション名の設定
            sap.setReason(options.getReason());
            sap.setLocation(options.getLocation());
            if (options.isVisible()) {
                createVisbleSignatureImage(sap, options);
            }
            pdfStamperWrapper.setEnforcedModificationDate(Calendar.getInstance());

            // OpenPDFの標準実装では、ハッシュアルゴリズムがSHA-1が固定になってしまうため
            // ハッシュアルゴリズムをSHA-256の明示的な設定の上書きのための拡張実装をする
            DefaultPdfSignature sig = new DefaultPdfSignature(digitalSignatureConfig.getPkcs12().getHashAlgorithm());
            sig.setSignInfo(key, chain, null);
            sig.put(PdfName.M, new PdfDate(new GregorianCalendar()));
            sap.setCryptoDictionary(sig);
            // PDFのセキュリティ設定を行う
            pdfStamperWrapper.setEncryption(
                    originalPdfReader.is128Key() ? PdfWriter.STANDARD_ENCRYPTION_128 : PdfWriter.STANDARD_ENCRYPTION_40,
                    options.getUserPassword(), options.getOwnerPassword(), originalPdfReader.getPermissions());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9003);
        }
    }

    /**
     * PDFに表示する可視署名の署名イメージを作成する
     * 
     * @param sap PdfSignatureAppearance
     */
    private void createVisbleSignatureImage(final PdfSignatureAppearance sap, final SignOptions options) {
        float[] rect = options.getVisibleSignRect();
        // OpenPDFの座標系は左下が原点でY軸が上方向に伸びる模様なので、Y座標を変換する
        sap.setVisibleSignature(new Rectangle(rect[0], Y_AXIS_MAX_VALUE - rect[3], rect[2], Y_AXIS_MAX_VALUE - rect[1]),
                options.getVisibleSignPage());
        sap.setLayer2Text(options.getVisibleSignText());
        String imagePath = options.getVisibleSignImagePath();
        try {
            sap.setImage(Image.getInstance(imagePath));
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9006, imagePath);
        }
    }
}
