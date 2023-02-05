package com.example.fw.common.file;

import java.io.InputStream;

public interface ObjectStorageFileAccessor {

    void save(InputStream inputStream, String targetFilePath);
}
