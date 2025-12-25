package com.example.fw.common.reports;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import lombok.Builder;

/**
 * Reportインタフェースのメモリ保存での実装クラス
 */
@Builder
public class InMemoryReport implements Report {
    // byte[]データ
    private final byte[] data;

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long getSize() {
        return data.length;
    }

}
