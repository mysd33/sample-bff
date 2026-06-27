package com.example.fw.common.async.config;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/// SQS本番向けの設定クラス
@Profile("production")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SQSCommonConfigurationProperties.class})
public class SQSCommonProdConfig {

    private final SQSCommonConfigurationProperties sqsCommonConfigurationProperties;

    /// SQSClientの定義
    @Profile("!xray")
    @Bean
    SqsClient sqsClient() {
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()//
            //　標準リトライ戦略
            .overrideConfiguration(o -> o.retryStrategy(RetryMode.STANDARD))
            .httpClientBuilder(ApacheHttpClient.builder()
                .maxConnections(sqsCommonConfigurationProperties.getMaxConnections())
                .connectionTimeout(
                    Duration.ofMillis(sqsCommonConfigurationProperties.getConnectionTimeout()))
            )//
            .region(region)//
            .build();
    }

    /// SQSClientの定義(X-Ray SDK ）<br>
    ///
    /// @deprecated X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    SqsClient sqsClientWithXRay() {
        Region region = Region.of(sqsCommonConfigurationProperties.getRegion());
        return SqsClient.builder()
            //　標準リトライ戦略
            .overrideConfiguration(o -> o.retryStrategy(RetryMode.STANDARD)
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .addExecutionInterceptor(new TracingInterceptor())
            )
            .httpClientBuilder(ApacheHttpClient.builder()
                .maxConnections(sqsCommonConfigurationProperties.getMaxConnections())
                .connectionTimeout(
                    Duration.ofMillis(sqsCommonConfigurationProperties.getConnectionTimeout()))
            )//
            .region(region).build();
    }
}
