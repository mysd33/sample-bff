package com.example.fw.common.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;

/**
 * 
 * SQS Local起動の設定クラス（開発時のみ）
 *
 */
@Configuration
@Profile("dev")
public class SQSCommonLocalConfig {
    private static final String HTTP_LOCALHOST = "http://localhost:";
    private static final String ELASTICMQ = "elasticmq";

    // SQS Local起動時のポート
    @Value("${aws.sqslocal.port}")
    private String port;

    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSConnectionFactoryの定義(X-Rayトレーシングなし）
     */
    @Profile("!xray")
    @Bean
    public SQSConnectionFactory sqsConnectionFactoryLocalWithoutXRay(ProviderConfiguration providerConfiguration) {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(HTTP_LOCALHOST + port, ELASTICMQ));
        return new SQSConnectionFactory(providerConfiguration, builder);        
    }

    /**
     * ElastiqMQ(SQSLocal)起動する場合のSQSConnectionFactoryの定義(X-Rayトレーシングあり）
     */
    @Profile("xray")
    @Bean
    public SQSConnectionFactory sqsConnectionFactoryLocalWithXRay(ProviderConfiguration providerConfiguration) {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                // 個別にSQSへのAWS SDKの呼び出しをトレーシングできるように設定
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .withEndpointConfiguration(new EndpointConfiguration(HTTP_LOCALHOST + port, ELASTICMQ));
        return new SQSConnectionFactory(providerConfiguration, builder);
    }

}
