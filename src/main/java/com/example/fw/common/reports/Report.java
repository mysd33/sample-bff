package com.example.fw.common.reports;

import java.io.InputStream;

import lombok.Builder;
import lombok.Value;

/**
 * 帳票クラス
 */
@Builder
@Value
public class Report {
    // InputStreamデータ
    private final InputStream inputStream;
    // データサイズ
    private final long size;

}
