package com.example.fw.common.async.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 
 * SQSのプロパティクラス
 *
 */
@Data
@ConfigurationProperties(prefix = "aws.sqs")
public class SQSCommonConfigurationProperties {
    private String region = "ap-northeast-1";
    private int numberOfMessagesToPrefetch = 0;
    private SQSLocalProperties sqslocal;
    
    @Data
    public static class SQSLocalProperties {
        private int port;
    }

}
