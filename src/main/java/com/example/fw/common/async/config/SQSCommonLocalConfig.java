package com.example.fw.common.async.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * 
 * SQS Local起動の設定クラス（開発時のみ）
 *
 */
@Profile("dev")
@Configuration
@EnableConfigurationProperties({ SQSCommonConfigurationProperties.class })
public class SQSCommonLocalConfig {
    private static final String HTTP_LOCALHOST = "http://localhost:";

    @Autowired
    private SQSCommonConfigurationProperties sqsCommonConfigurationProperties;

    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義(X-Rayトレーシングなし）
     */
    @Profile("!xray")
    @Bean
    public SqsClient sqsClientWithoutXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("dummy", "dummy");
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()//
                .httpClientBuilder((ApacheHttpClient.builder()))//
                .region(region)//
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(HTTP_LOCALHOST + sqsCommonConfigurationProperties.getSqslocal().getPort()))
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
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()                
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .httpClientBuilder((ApacheHttpClient.builder()))//
                .region(region)//
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(HTTP_LOCALHOST + sqsCommonConfigurationProperties.getSqslocal().getPort()))
                .build();
    }

}
