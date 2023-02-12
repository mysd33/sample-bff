package com.example.fw.common.objectstorage.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.example.fw.common.objectstorage.BucketCreateInitializer;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.S3ObjectStorageFileAccessor;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * 
 * S3が開発環境上でのローカルサーバFake（s3rver）実行に置き換える設定クラス
 *
 */
@Profile("dev")
@ConditionalOnProperty(prefix = "aws.s3.localfake", name = "type", havingValue = "s3rver")
@Configuration
public class S3LocalS3rverFakeConfig {
    /**
     * S3のプロパティクラス     
     */
    @Bean
    public S3ConfigurationProperties s3ConfigurationProperties() {
        return new S3ConfigurationProperties();
    }
    

    /**
     * オブジェクトストレージアクセスクラス
     */
    @Bean
    public ObjectStorageFileAccessor objectStorageFileAccessor(S3Client s3Client) {
        return new S3ObjectStorageFileAccessor(s3Client, s3ConfigurationProperties().getBucket());
    }

    /**
     * S3クライアント（X-Rayトレースなし）
     */
    @Profile("!xray")
    @Bean
    public S3Client s3ClientWithoutXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("S3RVER", "S3RVER");
        
        Region region = Region.of(s3ConfigurationProperties().getRegion());
        // @formatter:off
        return S3Client.builder()                
                .region(region)       
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("http://localhost:" + s3ConfigurationProperties().getLocalfake().getPort()))
                //S3rverの場合、putしたファイルにchunk-signatureが入ってしまうため対処策として設定
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false).build())
                .build();        
        // @formatter:on
    }

    /**
     * S3クライアント（X-Rayトレースあり）
     */
    @Profile("xray")
    @Bean
    public S3Client s3ClientWithXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("S3RVER", "S3RVER");
        
        Region region = Region.of(s3ConfigurationProperties().getRegion());
        // @formatter:off
        return S3Client.builder()                
                .region(region)       
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("http://localhost:" + s3ConfigurationProperties().getLocalfake().getPort()))
                //S3rverの場合、putしたファイルにchunk-signatureが入ってしまうため対処策として設定
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false).build())
                // 個別にDynamoDBへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .build();        
        // @formatter:on
    }        
    
    /**
     * バケット初期作成クラス
     * 
     */
    @Bean
    public BucketCreateInitializer bucketCreateInitializer(S3Client s3Client) {
        return new BucketCreateInitializer(s3Client, s3ConfigurationProperties().getBucket());
    }

}
