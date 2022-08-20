package com.example.bff;

import javax.jms.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.example.fw.common.async.repository.JobRequestRepository;
import com.example.fw.common.async.repository.JobRequestRepositoryImpl;

/**
 * SQSの設定クラス
 */
@Configuration
public class SQSConfig {
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
	 * JMSTemplateの定義
	 * @param connectionFactory
	 */
	@Bean
	public JmsTemplate defaultJmsTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
		return jmsTemplate;
	}

	/**
	 * JobRequestRepository（非同期実行）の設定
	 * @param jmsTemplate
	 */
	@Bean
	public JobRequestRepository jobRequestRepository(JmsTemplate jmsTemplate) {
		return new JobRequestRepositoryImpl(jmsTemplate);
	}
}
