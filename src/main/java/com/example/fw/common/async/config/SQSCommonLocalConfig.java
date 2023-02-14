package com.example.fw.common.async.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * 
 * SQS Local起動の設定クラス（開発時のみ）
 *
 */
@Configuration
@Profile("dev")
public class SQSCommonLocalConfig {
    private static final String HTTP_LOCALHOST = "http://localhost:";
    // SQS Local起動時のポート
    @Value("${aws.sqs.sqslocal.port}")
    private String port;
    // ダミーのリージョン
    @Value("${aws.sqs.region:ap-northeast-1}")
    private String region;
    
    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義(X-Rayトレーシングなし）
     */
    @Profile("!xray")
    @Bean
    public SqsClient sqsClientWithoutXRay() {
        // ダミーのクレデンシャル        
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("dummy", "dummy");
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(HTTP_LOCALHOST + port))
                .build();                        
    }

    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義(X-Rayトレーシングあり）
     */
    @Profile("xray")
    @Bean
    public SqsClient sqsClientFactoryWithXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("dummy", "dummy");
        return SqsClient.builder()
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(HTTP_LOCALHOST + port))
                .build();        
    }

}
