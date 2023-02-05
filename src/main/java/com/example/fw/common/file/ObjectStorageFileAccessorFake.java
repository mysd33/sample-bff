package com.example.fw.common.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.message.CommonFrameworkMessageIds;

/**
 * 
 * ローカルファイルシステムへアクセスするObjectFileAccessorのFake
 *
 */
public class ObjectStorageFileAccessorFake implements ObjectStorageFileAccessor {
    //ローカル保存する際のファイルパス
    @Value("${objectstorage.localfake.baseDir}")
    private String baseDir;
        
    @Override
    public void save(InputStream inputStream, String targetFilePath) {
        try {
            FileUtils.copyInputStreamToFile(inputStream, Path.of(baseDir, targetFilePath).toFile());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
        }
    }
}
