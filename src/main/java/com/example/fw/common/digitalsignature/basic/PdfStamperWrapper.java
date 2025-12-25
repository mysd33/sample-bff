package com.example.fw.common.digitalsignature.basic;

import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.Map;

import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfSigGenericPKCS;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;

import lombok.RequiredArgsConstructor;

/**
 * PdfStamperのラッパークラス<br>
 * 
 * 標準実装では、デフォルト実装はダイジェストアルゴリズムがSHA-1固定にされてしまう。
 * このためSHA-256等の明示的な設定の上書きのための拡張実装をするためのクラス
 */
@RequiredArgsConstructor
public class PdfStamperWrapper implements AutoCloseable {
    // 実際のPdfStamper
    private final PdfStamper stamper;

    /**
     * PdfStamplerのPdfSignatureAppearanceを取得する<br>
     */
    public PdfSignatureAppearance getSignatureAppearance() {
        return stamper.getSignatureAppearance();
    }

    /**
     * PdfStamplerのsetEnforcedModificationDateを呼び出す<br>
     * 
     * @param modificationDate
     */
    public void setEnforcedModificationDate(final Calendar modificationDate) {
        stamper.setEnforcedModificationDate(modificationDate);
    }

    /**
     * PdfStamplerのsetEncryptionを呼び出す<br>
     * 
     * @param encryptionType
     * @param userPassword
     * @param ownerPassword
     * @param permissions
     * @throws DocumentException
     */
    public void setEncryption(final int encryptionType, final String userPassword, final String ownerPassword,
            final int permissions) throws DocumentException {
        stamper.setEncryption(encryptionType, userPassword, ownerPassword, permissions);
    }

    @Override
    public void close() throws DocumentException, IOException {
        // 標準実装のPdfStamperのclose()は呼び出すと、
        // close()メソッド内で、sap.preClose()で、NullPointerExceptionが発生するため実装を置き換え
        PdfSignatureAppearance sap = stamper.getSignatureAppearance();
        PdfSigGenericPKCS sig = (PdfSigGenericPKCS) sap.getCryptoDictionary();
        PdfString contents = (PdfString) sig.get(PdfName.CONTENTS);
        PdfLiteral lit = new PdfLiteral(
                (contents.toString().length() + (PdfName.ADOBE_PPKLITE.equals(sap.getFilter()) ? 0 : 64)) * 2 + 2);
        Map<PdfName, Integer> exclusionSize = Map.of(PdfName.CONTENTS, lit.getPosLength());
        sap.preClose(exclusionSize);
        int totalBuf = (lit.getPosLength() - 2) / 2;
        byte[] buf = new byte[8192];
        int n;
        InputStream inp = sap.getRangeStream();
        try {
            while ((n = inp.read(buf)) > 0) {
                sig.getSigner().update(buf, 0, n);
            }
        } catch (SignatureException se) {
            throw new ExceptionConverter(se);
        }
        buf = new byte[totalBuf];
        byte[] bsig = sig.getSignerContents();
        System.arraycopy(bsig, 0, buf, 0, bsig.length);
        PdfString str = new PdfString(buf);
        str.setHexWriting(true);
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.CONTENTS, str);
        sap.close(dic);
        stamper.getReader().close();
    }

}
