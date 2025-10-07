package com.example.fw.common.objectstorage.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.example.fw.common.objectstorage.BucketCreateInitializer;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.S3ObjectStorageFileAccessor;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * 
 * S3が開発環境上でのローカルサーバFake（MinIO）実行に置き換える設定クラス
 *
 */
@Profile("dev")
@ConditionalOnProperty(prefix = S3ConfigurationProperties.LOCALFAKE_PROPERTY_PREFIX, name = "type", havingValue = "minio")
@EnableConfigurationProperties({ S3ConfigurationProperties.class })
@Configuration
@RequiredArgsConstructor
public class S3LocalMinioFakeConfig {
    private final S3ConfigurationProperties s3ConfigurationProperties;

    /**
     * オブジェクトストレージアクセスクラス
     */
    @Bean
    ObjectStorageFileAccessor objectStorageFileAccessor(S3Client s3Client) {
        return new S3ObjectStorageFileAccessor(s3Client, s3ConfigurationProperties.getBucket());
    }

    /**
     * S3クライアント（X-Rayトレースなし）
     */
    @Profile("!xray")
    @Bean
    S3Client s3ClientWithoutXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                s3ConfigurationProperties.getLocalfake().getAccessKeyId(),
                s3ConfigurationProperties.getLocalfake().getSecretAccessKey());

        Region region = Region.of(s3ConfigurationProperties.getRegion());
        // @formatter:off
        return S3Client.builder()
                .httpClientBuilder((ApacheHttpClient.builder()))
                .region(region)       
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("http://localhost:" + s3ConfigurationProperties.getLocalfake().getPort()))
                //MinIOはデフォルトPath-Styleのため
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true).build())
                .build();        
        // @formatter:on
    }

    /**
     * S3クライアント（X-Rayトレースあり）
     */
    @Profile("xray")
    @Bean
    S3Client s3ClientWithXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                s3ConfigurationProperties.getLocalfake().getAccessKeyId(),
                s3ConfigurationProperties.getLocalfake().getSecretAccessKey());

        Region region = Region.of(s3ConfigurationProperties.getRegion());
        // @formatter:off
        return S3Client.builder()                
                .region(region)
                .httpClientBuilder((ApacheHttpClient.builder()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("http://localhost:" + s3ConfigurationProperties.getLocalfake().getPort()))
                // 個別にDynamoDBへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                //MinIOはデフォルトPath-Styleのため
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true).build())
                .build();        
        // @formatter:on
    }

    /**
     * バケット初期作成クラス
     * 
     */
    @Bean
    BucketCreateInitializer bucketCreateInitializer(S3Client s3Client) {
        return new BucketCreateInitializer(s3Client, s3ConfigurationProperties.getBucket());
    }

}
