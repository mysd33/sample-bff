package com.example.fw.common.file;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadObject {
    private InputStream inputStream;    
    private long size;
    private String targetFilePath;
}
