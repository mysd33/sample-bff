package com.example.fw.common.objectstorage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties({S3ConfigurationProperties.class})
@Configuration
public class S3ProdConfig {    
    @Autowired
    private S3ConfigurationProperties s3ConfigurationProperties;    
      
    /**
     * オブジェクトストレージアクセスクラス
     */        
    @Bean
    public ObjectStorageFileAccessor objectStorageFileAccessor(S3Client s3Client) {
        return new S3ObjectStorageFileAccessor(s3Client, s3ConfigurationProperties.getBucket());
    } 
    
    /**
     * S3クライアント
     */
    @Bean
    public S3Client s3Client() {
        Region region = Region.of(s3ConfigurationProperties.getRegion());
        return S3Client.builder()
                .region(region)
                .build();        
    }

}
