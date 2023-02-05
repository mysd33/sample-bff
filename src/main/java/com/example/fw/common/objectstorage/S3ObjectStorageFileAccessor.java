package com.example.fw.common.objectstorage;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
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
}
