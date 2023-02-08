package com.example.fw.common.objectstorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.S3ObjectStorageFileAccessor;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * 
 * S3の本番環境用設定クラス
 *
 */
@Profile("production")  
@Configuration
public class S3ProdConfig {    
    @Value("${aws.s3.region}")
    private String region;
    @Value("${aws.s3.bucket}")
    private String bucket;
      
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
        return S3Client.builder()
                .region(Region.of(region))
                .build();        
    }

}
