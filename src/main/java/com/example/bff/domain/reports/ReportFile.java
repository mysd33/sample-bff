package com.example.bff.domain.reports;

import java.io.InputStream;

import lombok.Builder;
import lombok.Value;

/**
 * 帳票ファイルクラス
 */
@Builder
@Value
public class ReportFile {
    // InputStreamデータ
    private final InputStream inputStream;
    // ファイルサイズ
    private final long fileSize;
    // ファイル名
    private final String fileName;

}
