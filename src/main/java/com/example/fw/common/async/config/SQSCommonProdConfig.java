package com.example.fw.common.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazonaws.xray.interceptors.TracingInterceptor;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * SQS本番向けの設定クラス
 */
@Configuration
@Profile("production")
public class SQSCommonProdConfig {
    @Value("${aws.sqs.region}")
    private String region;

    /**
     * SQSClientの定義(X-Rayトレーシングなし）
     */
    @Profile("!xray")
    @Bean
    public SqsClient sqsClientWithoutXRay() {
        return SqsClient.builder().region(Region.of(region)).build();
    }

    /**
     * SQSConnectionFactoryの定義(X-Rayトレーシングあり）
     */
    @Profile("xray")
    @Bean
    public SqsClient sqsClientWithXRay(ProviderConfiguration providerConfiguration) {
        return SqsClient.builder()
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .region(Region.of(region))
                .build();
    }

}
