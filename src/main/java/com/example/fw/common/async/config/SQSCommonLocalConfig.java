package com.example.fw.common.async.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@EnableConfigurationProperties({ SQSCommonConfigurationProperties.class })
public class SQSCommonLocalConfig {
    private static final String HTTP_LOCALHOST = "http://localhost:";

    private final SQSCommonConfigurationProperties sqsCommonConfigurationProperties;

    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義
     */
    @Profile("!xray")
    @Bean
    SqsClient sqsClient() {
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
     * ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義(X-Ray SDK)<br>
     * 
     * @deprecated X-Ray SDKは2027 年 2 月 25 日にサポート終了となるため削除予定
     */
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    SqsClient sqsClientWithXRay() {
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
