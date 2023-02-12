package com.example.fw.common.objectstorage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    // ローカル保存する際のファイルパス
    @Value("${aws.s3.localfake.baseDir}")
    private String baseDir;

    @Override
    public void upload(UploadObject uploadObject) {
        Path destinationPath = Path.of(baseDir).resolve(uploadObject.getTargetFilePath());

        try {
            FileUtils.copyInputStreamToFile(uploadObject.getInputStream(), destinationPath.toFile());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
        }
    }

    @Override
    public DownloadObject download(String targetFilePath) {
        Path destinationPath = Path.of(baseDir).resolve(targetFilePath);
        String fileName = destinationPath.getFileName().toString();
        try {
            return DownloadObject.builder()
                    .inputStream(new BufferedInputStream(new FileInputStream(destinationPath.toFile())))
                    .targetFilePath(targetFilePath)
                    .fileName(fileName)
                    .build();
        } catch (FileNotFoundException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
        }                
    }
}
