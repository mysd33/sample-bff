package com.example.fw.common.reports.config;

import com.example.fw.common.constants.FrameworkConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// 帳票出力関連の設定を保持するプロパティクラス
@Data
@ConfigurationProperties(prefix = ReportsConfigurationProperties.PROPERTY_PREFIX)
public class ReportsConfigurationProperties {

    // 帳票出力機能のプロパティのプレフィックス
    static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "report";
    // コンパイル済の帳票様式を保存するデフォルトの一時ディレクトリ名
    private static final String DEFAULT_TEMP_JASPER_DIR = "jasper";

    /// コンパイル済の帳票様式を保存する一時ディレクトリのパス
    private String jasperFileTmpdir = DEFAULT_TEMP_JASPER_DIR;

    /// 128Bit暗号化を使用するか
    ///
    /// @see <a
    /// href="https://jasperreports.sourceforge.net/config.reference.html#net.sf.jasperreports.export.pdf.128.bit.key">JapserReportドキュメント</a>
    private boolean is128bitKey = true;

    /// 暗号化設定されたPDFの場合のAP全体共通の権限拒否設定 PDFOptionsで個別設定されていない場合に使用される
    /// 複数指定する場合には、「|」（パイプ）で区切る
    ///
    /// - ALL - all user permissions are denied（全てのユーザ権限）
    /// - ASSEMBLY - assembly permission denied（ページの挿入、削除、回転、PDFのページに関する変更）
    /// - COPY - copy permission denied（内容のコピー）
    /// - DEGRADED_PRINTING - degraded printing permission denied（低解像度の印刷）
    /// - FILL_IN - fill in forms permission denied（フォームフィールドの入力）
    /// - MODIFY_ANNOTATIONS - modify annotation permission denied（注釈の作成、編集）
    /// - MODIFY_CONTENTS - modify contents permission denied（文書のの変更）
    /// - PRINTING - print permission denied（印刷）
    /// - SCREENREADERS - screen readers permission allowed（スクリーンリーダのアクセス） </ul
    ///
    /// @see <a
    /// href="https://jasperreports.sourceforge.net/config.reference.html#net.sf.jasperreports.export.pdf.permissions.denied">JasperReportドキュメント</a>
    private String pdfPermissionDenied = null;
}
