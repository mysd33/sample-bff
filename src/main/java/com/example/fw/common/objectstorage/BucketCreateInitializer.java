package com.example.fw.common.objectstorage;

import javax.annotation.PostConstruct;

import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

/**
 * 
 * バケットを初期作成するクラス
 *
 */
@RequiredArgsConstructor
@Slf4j
public class BucketCreateInitializer {    
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final S3Client s3Client;
    private final String bucketName;

    @PostConstruct
    public void startup() {
        //バケットの存在確認
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);        
        boolean isExsits = false;
        for (Bucket bucket : listBucketsResponse.buckets()) {
            if (bucketName.equals(bucket.name())) {
                isExsits = true;
                break;
            }
        }
        if (isExsits) {
            appLogger.debug("バケット{}は存在します", bucketName);
            return;
        }
        
        //バケット作成                        
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.createBucket(bucketRequest);        
        appLogger.debug("バケット{}を作成しました", bucketName);
    }
    

}
