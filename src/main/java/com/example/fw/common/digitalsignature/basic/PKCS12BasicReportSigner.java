package com.example.fw.common.digitalsignature.basic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicReference;

import com.example.fw.common.digitalsignature.ReportSigner;
import com.example.fw.common.digitalsignature.SignOptions;
import com.example.fw.common.digitalsignature.config.DigitalSignatureConfigurationProperties;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.reports.DefaultReport;
import com.example.fw.common.reports.Report;
import com.example.fw.common.reports.ReportsConstants;
import com.example.fw.common.reports.config.ReportsConfigurationProperties;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PKCS#12形式のキーストアを使用してPDFに基本的な電子署名を付与するクラス
 * 
 * このクラスは、PKCS#12形式のキーストアから秘密鍵と証明書を読み込み、PDFに電子署名を付与します。
 * 
 * @see ReportSigner
 */
@Slf4j
@RequiredArgsConstructor
public class PKCS12BasicReportSigner implements ReportSigner {
    private static final String PKCS12 = "pkcs12";
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final ReportsConfigurationProperties config;
    private final DigitalSignatureConfigurationProperties digitalSignatureConfig;

    // PDFの一時保存ファイルのディレクトリ（パスを初期化設定後、定期削除のための別スレッドで参照されるためAtomicReferenceにしておく）
    private final AtomicReference<Path> pdfTempPath = new AtomicReference<>();

    /**
     * 初期化処理
     * 
     */
    @PostConstruct
    public void init() {
        // 帳票を一時保存する一時ディレクトリを作成する
        pdfTempPath.set(Path.of(ReportsConstants.TMP_DIR, config.getReportTmpdir()));
        appLogger.debug("pdfTempPath: {}", pdfTempPath);
        // 一時ディレクトリが存在しない場合は作成する
        pdfTempPath.get().toFile().mkdirs();
    }

    @Override
    public Report sign(Report originalReport) {
        return sign(originalReport, SignOptions.builder().build());
    }

    @Override
    public Report sign(Report originalReport, SignOptions options) {
        // デフォルトの署名処理としてOpenPDFを使用して、PDFに電子署名を付与する実装例
        // https://javadoc.io/doc/com.github.librepdf/openpdf/1.3.43/com/lowagie/text/pdf/PdfStamper.html#createSignature-com.lowagie.text.pdf.PdfReader-java.io.OutputStream-char-
        PdfReader originalPdfReader = null;
        try {
            originalPdfReader = new PdfReader(originalReport.getInputStream());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9001);
        }
        // メモリを極力使わないよう、PDFのファイルサイズが大きい場合も考慮し一時ファイルに出力してInputStreamで返却するようにする
        Path signedPdfTempFilePath = null;
        try {
            signedPdfTempFilePath = Files.createTempFile(pdfTempPath.get(), ReportsConstants.PDF_TEMP_FILE_PREFIX,
                    ReportsConstants.PDF_FILE_EXTENSION);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9002);
        }
        try (FileOutputStream fos = new FileOutputStream(signedPdfTempFilePath.toFile())) {
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
                    .file(signedPdfTempFilePath.toFile()).build();
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
    private void doSign(PdfReader originalPdfReader, FileOutputStream fos, PrivateKey key, Certificate[] chain,
            SignOptions options) {
        try (PdfStamperWrapper pdfStamperWrapper = new PdfStamperWrapper(
                PdfStamper.createSignature(originalPdfReader, fos, '\0'))) {
            PdfSignatureAppearance sap = pdfStamperWrapper.getSignatureAppearance();
            sap.setReason(options.getReason());
            sap.setLocation(options.getLocation());
            if (options.isVisible()) {
                createVisbleSignatureImage(sap, options);
            }
            pdfStamperWrapper.setEnforcedModificationDate(Calendar.getInstance());

            // OpenPDFの標準実装では、ハッシュアルゴリズムがSHA-1が固定になってしまうため
            // ハッシュアルゴリズムをSHA-256の明示的な設定の上書きのための拡張実装をする
            DefaultPdfSignature sig = new DefaultPdfSignature(digitalSignatureConfig.getHashAlgorithm());
            sig.setSignInfo(key, chain, null);
            sig.put(PdfName.M, new PdfDate(new GregorianCalendar()));
            sap.setCryptoDictionary(sig);
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9003);
        }
    }

    /**
     * PDFに表示する可視署名の署名イメージを作成する
     * 
     * @param sap PdfSignatureAppearance
     */
    private void createVisbleSignatureImage(PdfSignatureAppearance sap, SignOptions options) {
        float[] rect = options.getVisibleSignRect();
        sap.setVisibleSignature(new Rectangle(rect[0], rect[1], rect[2], rect[3]), options.getVisibleSignPage());
        sap.setLayer2Text(options.getVisibleSignText());
        String imagePath = options.getVisibleSignImagePath();
        try {
            sap.setImage(Image.getInstance(imagePath));
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9006, imagePath);
        }
    }
}
