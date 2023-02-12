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

    // 参考
    // https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/java_s3_code_examples.html

    @Override
    public void upload(UploadObject uploadObject) {
        // @formatter:off
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(uploadObject.getPrefix())
                .build(),
                RequestBody.fromInputStream(uploadObject.getInputStream(), uploadObject.getSize()));
        // @formatter:on
    }

    @Override
    public DownloadObject download(String prefix) {
        // @formatter:off
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(prefix)
                .build();        
        // @formatter:on        
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
        String fileName = Path.of(prefix).getFileName().toString();
        return DownloadObject.builder().inputStream(responseInputStream).prefix(prefix).fileName(fileName).build();

    }
}
