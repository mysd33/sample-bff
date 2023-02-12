package com.example.fw.common.objectstorage;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * オブジェクトストレージからダウンロードしたオブジェクトクラス
 *
 */
@Data
@Builder
public class DownloadObject {
    private InputStream inputStream;
    private String targetFilePath;
    private String fileName;
}
