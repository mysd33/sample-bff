package com.example.fw.common.objectstorage.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.fw.common.objectstorage.BucketCreateInitializer;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.S3ObjectStorageFileAccessor;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * 
 * S3が開発環境上でのローカルサーバFake（s3rver）実行に置き換える設定クラス
 *
 */
@Profile("dev")  
@ConditionalOnProperty(prefix = "aws.s3.localfake", name = "type", havingValue = "s3rver")
@Configuration
public class S3LocalS3rverFakeConfig {    
    @Value("${aws.s3.region:ap-northeast-1}")
    private String region;
    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.s3.port:4568}")
    private String port;
      
    /**
     * オブジェクトストレージアクセスクラス
     */        
    @Bean
    public ObjectStorageFileAccessor objectStorageFileAccessor(S3Client s3Client) {
        return new S3ObjectStorageFileAccessor(s3Client, bucket);
    } 
    
    /**
     * S3クライアント
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                "S3RVER",
                "S3RVER");
        return S3Client.builder()                
                .region(Region.of(region))       
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("http://localhost:" + port))
                .build();        
    }

    /**
     * バケット初期作成クラス
     * 
     */
    @Bean
    public BucketCreateInitializer bucketCreateInitializer() {
        return new BucketCreateInitializer(s3Client(), bucket);       
    }
    


}
