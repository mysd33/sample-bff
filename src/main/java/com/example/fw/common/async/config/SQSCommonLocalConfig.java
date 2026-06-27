package com.example.fw.common.async.config;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/// SQS Local起動の設定クラス（開発時のみ）
@Profile("dev")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({SQSCommonConfigurationProperties.class})
public class SQSCommonLocalConfig {

    private static final String HTTP_LOCALHOST = "http://localhost:";

    private final SQSCommonConfigurationProperties sqsCommonConfigurationProperties;

    /// ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義
    @Profile("!xray")
    @Bean
    SqsClient sqsClient() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("dummy", "dummy");
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
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .endpointOverride(URI.create(
                HTTP_LOCALHOST + sqsCommonConfigurationProperties.getSqslocal().getPort()))
            .build();
    }

    /// ElastiqMQ(SQSLocal)起動する場合のSQSClientの定義(X-Ray SDK)<br>
    ///
    /// @deprecated X-Ray SDKは2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    SqsClient sqsClientWithXRay() {
        // ダミーのクレデンシャル
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("dummy", "dummy");
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
            .region(region)//
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .endpointOverride(URI.create(
                HTTP_LOCALHOST + sqsCommonConfigurationProperties.getSqslocal().getPort()))
            .build();
    }

}
