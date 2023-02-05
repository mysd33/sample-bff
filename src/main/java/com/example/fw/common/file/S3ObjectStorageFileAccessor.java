package com.example.fw.common.file;

import java.io.InputStream;

/**
 * ObjectStorageFileAccessorのS3実装 
 *
 */
public class S3ObjectStorageFileAccessor implements ObjectStorageFileAccessor {
    @Override
    public void save(InputStream inputStream, String targetFilePath) {
        //TODO: 実装
        throw new UnsupportedOperationException();
    }
}
