package com.example.fw.common.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

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

	@Value("${aws.sqslocal.port}")
	private String port;

	/**
	 * ElastiqMQ(SQSLocal)起動する場合のSQSConnectionFactoryの定義
	 */
	@Bean
	public SQSConnectionFactory sqsConnectionFactoryLocal() {
		AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration(HTTP_LOCALHOST + port, ELASTICMQ));
		SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), builder);
		return connectionFactory;
	}

}
