package com.example.fw.common.async.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.interceptors.TracingInterceptor;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * SQS本番向けの設定クラス
 */
@Profile("production")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SQSCommonConfigurationProperties.class})
public class SQSCommonProdConfig {
    private final SQSCommonConfigurationProperties sqsCommonConfigurationProperties;

    /**
     * SQSClientの定義(X-Rayトレーシングなし）
     */
    @Profile("!xray")
    @Bean
    SqsClient sqsClientWithoutXRay() {
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()//
                .httpClientBuilder((ApacheHttpClient.builder()))//
                .region(region)//                
                .build();
    }

    /**
     * SQSConnectionFactoryの定義(X-Rayトレーシングあり）
     */
    @Profile("xray")
    @Bean
    SqsClient sqsClientWithXRay() {
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .httpClientBuilder((ApacheHttpClient.builder()))//                
                .region(region)
                .build();
    }

}
