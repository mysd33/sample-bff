package com.example.fw.common.objectstorage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseBytes;
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
                    .key(uploadObject.getPrefix())
                    .build(),
                RequestBody.fromInputStream(uploadObject.getInputStream(), uploadObject.getSize()));
    }
    
    @Override
    public DownloadObject download(String prefix) {        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(prefix)
                .build();
        //https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/java_s3_code_examples.html
        //TODO :S3TransferManagerを使って直接一時ディレクトリに、ローカルファイルにダウンロードする実装方法に変える
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        InputStream responseInputStream = new BufferedInputStream(new ByteArrayInputStream(objectBytes.asByteArray()));
        String fileName = Path.of(prefix).getFileName().toString();
                        
        
        return DownloadObject.builder()
                .inputStream(responseInputStream)   
                .prefix(prefix)
                .fileName(fileName)
                .build();
        
    }
}
