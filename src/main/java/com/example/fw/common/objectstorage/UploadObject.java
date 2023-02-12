package com.example.fw.common.objectstorage;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * オブジェクトストレージへアップロードするオブジェクトクラス
 *
 */
@Data
@Builder
public class UploadObject {
    private InputStream inputStream;    
    private long size;
    private String targetFilePath;
}
