package com.example.fw.common.reports;

/**
 * 帳票関連の定数を定義するクラス
 * 
 * このクラスは、帳票の一時保存ディレクトリやファイル名のプレフィックス、拡張子などの定数を定義します。
 */
public final class ReportsConstants {
    private ReportsConstants() {
    }

    // 一時ディレクトリのパス
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    // PDFの一時保存ファイルのプレフィックス
    public static final String PDF_TEMP_FILE_PREFIX = "report_tmp";
    // 署名付きPDFの一時保存ファイルのプレフィックス
    public static final String SIGNED_PDF_TEMP_FILE_PREFIX = "signed_report_tmp";
    // 拡張子
    public static final String PDF_FILE_EXTENSION = ".pdf";
    public static final String JASPER_FILE_EXTENSION = ".jasper";
    public static final String JRXML_FILE_EXTENSION = ".jrxml";

}
