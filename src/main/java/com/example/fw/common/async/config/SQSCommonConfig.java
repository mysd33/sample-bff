package com.example.fw.common.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.amazon.sqs.javamessaging.ProviderConfiguration;

/**
 * SQSの設定クラス
 */
@Configuration
public class SQSCommonConfig {
    // SQSのメッセージプリフェッチ数の設定
    @Value("${aws.sqs.numberOfMessagesToPrefetch:0}")
    private int numberOfMessagesToPrefetch;
    
    /**
     * JMSのメッセージコンバータの定義
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    /**
     * SQSConnectionFactoryのSQSのメッセージプリフェッチ数の設定
     */
    @Bean
    public ProviderConfiguration providerConfiguration() {
        return new ProviderConfiguration().withNumberOfMessagesToPrefetch(numberOfMessagesToPrefetch);
    }
}
