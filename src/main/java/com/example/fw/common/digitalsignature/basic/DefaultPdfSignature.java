package com.example.fw.common.digitalsignature.basic;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfSigGenericPKCS;

/**
 * 本機能のデフォルトのPdfSignature（PdfDictionary）実装クラス<br>
 * 
 * OpenPDF署名のデフォルト実装の場合、ハッシュアルゴリズムがSHA-1固定になってしまうため、
 * ハッシュアルゴリズムをSHA-256等で明示的な設定の上書きのため拡張をする
 */
public class DefaultPdfSignature extends PdfSigGenericPKCS {

    public DefaultPdfSignature(final String hashAlgorithm) {
        // https://www.antenna.co.jp/pdf/reference/PDFSingature.html
        // ISO 32000-1では、SubFilterとしてadbe.pkcs7.detachedを設定することが推奨されている。
        super(PdfName.ADOBE_PPKMS, PdfName.ADBE_PKCS7_DETACHED);
        // ハッシュアルゴリズムを明示的な設定の上書きのため拡張をする
        super.hashAlgorithm = hashAlgorithm;
    }

}
