package com.example.fw.common.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 帳票出力関連の設定を保持するプロパティクラス
 */
@Data
@ConfigurationProperties(prefix = "report")
public class ReportsConfigurationProperties {
    // コンパイル済の帳票様式を保存するデフォルトの一時ディレクトリ名
    private static final String DEFAULT_TEMP_JASPER_DIR = "jasper";
    // 帳票を一時保存するデフォルトの一時ディレクトリ名
    private static final String DEFAULT_TEMP_REPORT_DIR = "report";
    // PDFの一時保存ファイルを更新日時から何秒経過していたら削除対象とするか デフォルト60秒（1分）
    private static final int DEFAULT_DELETE_DURATION_SECONDS = 60;
    /**
     * コンパイル済の帳票様式を保存する一時ディレクトリのパス
     */
    private String jasperFileTmpdir = DEFAULT_TEMP_JASPER_DIR;

    /**
     * 帳票を一時保存する一時ディレクトリのパス
     */
    private String reportTmpdir = DEFAULT_TEMP_REPORT_DIR;

    /**
     * 帳票の一時保存ファイルの更新日時から何秒経過していたら削除対象とするか
     */
    private int deleteDurationSeconds = DEFAULT_DELETE_DURATION_SECONDS;

    /**
     * 128Bit暗号化を使用するか
     * 
     * @see https://jasperreports.sourceforge.net/config.reference.html#net.sf.jasperreports.export.pdf.128.bit.key
     */
    private boolean is128bitKey = true;

    /**
     * 暗号化設定されたPDFの場合のAP全体共通の権限拒否設定 PDFOptionsで個別設定されていない場合に使用される
     * 複数指定する場合には、「|」（パイプ）で区切る
     * <ul>
     * <li>ALL - all user permissions are denied（全てのユーザ権限）
     * <li>ASSEMBLY - assembly permission denied（ページの挿入、削除、回転、PDFのページに関する変更）
     * <li>COPY - copy permission denied（内容のコピー）
     * <li>DEGRADED_PRINTING - degraded printing permission denied（低解像度の印刷）
     * <li>FILL_IN - fill in forms permission denied（フォームフィールドの入力）
     * <li>MODIFY_ANNOTATIONS - modify annotation permission denied（注釈の作成、編集）
     * <li>MODIFY_CONTENTS - modify contents permission denied（文書のの変更）
     * <li>PRINTING - print permission denied（印刷）
     * <li>SCREENREADERS - screen readers permission allowed（スクリーンリーダのアクセス） </ul
     * 
     * @see https://jasperreports.sourceforge.net/config.reference.html#net.sf.jasperreports.export.pdf.permissions.denied
     * 
     */
    private String pdfPermissionDenied = null;
}
