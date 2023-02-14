package com.example.bff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import com.example.fw.common.async.config.SQSCommonConfigPackage;
import com.example.fw.common.async.repository.JobRequestRepository;
import com.example.fw.common.async.repository.JobRequestRepositoryImpl;

@Configuration
@ComponentScan(basePackageClasses = { SQSCommonConfigPackage.class })
public class SQSConfig {
    @Value("${delayed.batch.queue}")
    private String queueName;

    /**
     * JobRequestRepository（非同期実行）の設定
     * 
     * @param jmsTemplate
     */
    @Bean
    public JobRequestRepository jobRequestRepository(JmsTemplate jmsTemplate) {
        return new JobRequestRepositoryImpl(jmsTemplate, queueName);
    }
}
