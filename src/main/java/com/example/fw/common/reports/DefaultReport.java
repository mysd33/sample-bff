package com.example.fw.common.reports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lombok.Builder;
import lombok.Getter;

/**
 * Reportインタフェースのファイル保存での実装クラス
 */
@Builder
public class DefaultReport implements Report {
    // Fileデータ
    @Getter
    private final File file;

    @Override
    public InputStream getInputStream() {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            // Fileが見つからない場合はnullを返す
            return null;
        }
    }

    @Override
    public long getSize() {
        // Fileのサイズを取得
        return file.length();
    }

}
