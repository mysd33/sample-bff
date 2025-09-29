package com.example.fw.common.digitalsignature;

import com.example.fw.common.reports.Report;

/**
 * 帳票にデジタル署名を行うインターフェース。
 * 
 */
public interface ReportSigner {
    /**
     * PDF帳票にデジタル署名を生成するメソッド。
     * 
     * @param report  署名対象のPDF帳票
     * @param options 署名に関するオプション
     * @return 署名されたPDF帳票
     */
    Report sign(Report report);

    /**
     * PDF帳票にデジタル署名を生成するメソッド。
     * 
     * @param report  署名対象のPDF帳票
     * @param options 署名に関するオプション
     * @return 署名されたPDF帳票
     */
    Report sign(Report report, SignOptions options);

}
