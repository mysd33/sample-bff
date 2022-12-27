package com.example.fw.common.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * SQS本番向けの設定クラス
 */
@Configuration
@Profile("production")
public class SQSCommonProdConfig {
	@Value("${aws.sqs.region}")
	private String region;

	/**
	 * SQSConnectionFactoryの定義
	 */
	@Bean
	public SQSConnectionFactory sqsConnectionFactory() {
		AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard().withRegion(region);
		SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), builder);
		return connectionFactory;
	}

}
