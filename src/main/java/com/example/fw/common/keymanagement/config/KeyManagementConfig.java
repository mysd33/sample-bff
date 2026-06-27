package com.example.fw.common.keymanagement.config;

import com.example.fw.common.keymanagement.AWSKmsKeyManager;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.config.S3ConfigPackage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsAsyncClient;

@Configuration
@ComponentScan(basePackageClasses = S3ConfigPackage.class)
@EnableConfigurationProperties(KeyManagementConfigurationProperties.class)
public class KeyManagementConfig {

    /// KmsAsyncClientのBean定義
    @Bean
    KmsAsyncClient kmsAsyncClient(
        KeyManagementConfigurationProperties keyManagementConfigurationProperties) {
        return KmsAsyncClient.builder()
            //　標準リトライ戦略
            .overrideConfiguration(o -> o.retryStrategy(RetryMode.STANDARD))
            .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                .maxConcurrency(
                    keyManagementConfigurationProperties.getAwsKms().getMaxConnections())
                .connectionTimeout(java.time.Duration.ofMillis(2000))
            )//
            .region(Region.of(keyManagementConfigurationProperties.getAwsKms().getRegion()))//
            .build();
    }

    @Bean
    KeyManager keyManager(KmsAsyncClient kmsAsyncClient,
        ObjectStorageFileAccessor objectStorageFileAccessor,
        KeyManagementConfigurationProperties keyManagmentConfigurationProperties) {
        return new AWSKmsKeyManager(kmsAsyncClient, objectStorageFileAccessor,
            keyManagmentConfigurationProperties);
    }
}
