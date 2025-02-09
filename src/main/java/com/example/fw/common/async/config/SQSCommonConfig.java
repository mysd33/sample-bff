package com.example.fw.common.async.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;

import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * SQSの設定クラス
 */
@Configuration
@EnableConfigurationProperties({SQSCommonConfigurationProperties.class})
public class SQSCommonConfig {
  
    /**
     * JMSのメッセージコンバータの定義
     */
    @Bean
    MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    /**
     * SQSConnectionFactoryのSQSのメッセージプリフェッチ数の設定
     */
    @Bean
    ProviderConfiguration providerConfiguration(SQSCommonConfigurationProperties sqsCommonConfigurationProperties) {
        return new ProviderConfiguration().withNumberOfMessagesToPrefetch(sqsCommonConfigurationProperties.getNumberOfMessagesToPrefetch());
    }
    
    /**
     * SQSConnectionFactoryの定義
     */
    @Bean
    SQSConnectionFactory sqsConnectionFactory(ProviderConfiguration providerConfiguration, SqsClient sqsClient) {
        return new SQSConnectionFactory(providerConfiguration, sqsClient);
    }
}
