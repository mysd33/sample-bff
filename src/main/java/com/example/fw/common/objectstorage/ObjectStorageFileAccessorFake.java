package com.example.fw.common.objectstorage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.message.CommonFrameworkMessageIds;

import lombok.RequiredArgsConstructor;

/**
 * 
 * ローカルファイルシステムへアクセスするObjectFileAccessorのFake
 *
 */
@RequiredArgsConstructor
public class ObjectStorageFileAccessorFake implements ObjectStorageFileAccessor {
    private final String baseDir;

    @Override
    public void upload(UploadObject uploadObject) {
        Path destinationPath = Path.of(baseDir).resolve(uploadObject.getPrefix());

        try {
            FileUtils.copyInputStreamToFile(uploadObject.getInputStream(), destinationPath.toFile());
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
        }
    }

    @Override
    public DownloadObject download(String prefix) {
        Path destinationPath = Path.of(baseDir).resolve(prefix);
        String fileName = destinationPath.getFileName().toString();
        try {
            return DownloadObject.builder()
                    .inputStream(new BufferedInputStream(new FileInputStream(destinationPath.toFile())))
                    .prefix(prefix)
                    .fileName(fileName)
                    .build();
        } catch (FileNotFoundException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
        }                
    }
}
