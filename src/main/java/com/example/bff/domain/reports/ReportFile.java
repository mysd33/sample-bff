package com.example.bff.domain.reports;

import java.io.InputStream;

import lombok.Builder;
import lombok.Value;

/// 帳票ファイルクラス
@Builder
@Value
public class ReportFile {

    // InputStreamデータ
    InputStream inputStream;
    // ファイルサイズ
    long fileSize;
    // ファイル名
    String fileName;

}
