package com.example.fw.common.keymanagement.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.fw.common.keymanagement.AWSKmsKeyManager;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsAsyncClient;

@Configuration
@EnableConfigurationProperties(KeyManagementConfigurationProperties.class)
public class KeyManagmentConfig {

    /**
     * KmsAsyncClientのBean定義
     */
    @Bean
    KmsAsyncClient kmsAsyncClient(KeyManagementConfigurationProperties keyManagmentConfigurationProperties) {
        // KMSクライアントのビルド
        return KmsAsyncClient.builder().//
                region(Region.of(keyManagmentConfigurationProperties.getAwsKms().getRegion()))//
                .build();
    }

    @Bean
    KeyManager keyManager(KmsAsyncClient kmsAsyncClient, ObjectStorageFileAccessor objectStorageFileAccessor,
            KeyManagementConfigurationProperties keyManagmentConfigurationProperties) {
        return new AWSKmsKeyManager(kmsAsyncClient, objectStorageFileAccessor, keyManagmentConfigurationProperties);
    }
}
