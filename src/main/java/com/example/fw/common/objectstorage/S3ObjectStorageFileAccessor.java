package com.example.fw.common.objectstorage;

import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * ObjectStorageFileAccessorのS3実装
 *
 */
@RequiredArgsConstructor
public class S3ObjectStorageFileAccessor implements ObjectStorageFileAccessor {
    private final S3Client s3Client;
    private final String bucket;

    @Override
    public void upload(UploadObject uploadObject) {
        s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(uploadObject.getTargetFilePath())
                    .build(),
                RequestBody.fromInputStream(uploadObject.getInputStream(), uploadObject.getSize()));
    }
    
    @Override
    public DownloadObject download(String targetFilePath) {        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(targetFilePath)
                .build();
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
        
        String fileName = Path.of(targetFilePath).getFileName().toString();
        
        return DownloadObject.builder()
                .inputStream(responseInputStream)   
                .targetFilePath(targetFilePath)
                .fileName(fileName)
                .build();
        
    }
}
