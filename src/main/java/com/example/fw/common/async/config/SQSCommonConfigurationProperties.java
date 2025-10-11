package com.example.fw.common.async.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.fw.common.constants.FrameworkConstants;

import lombok.Data;

/**
 * 
 * SQSのプロパティクラス
 *
 */
@Data
@ConfigurationProperties(prefix = SQSCommonConfigurationProperties.PRPOPERTY_PREFIX)
public class SQSCommonConfigurationProperties {
    // 非同期実行依頼のプロパティのプレフィックス
    static final String PRPOPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "sqs";
    // リージョン（デフォルト: ap-northeast-1）
    private String region = "ap-northeast-1";
    // プリフェッチ数
    private int numberOfMessagesToPrefetch = 0;
    // ローカルSQSの設定
    private SQSLocalProperties sqslocal;

    @Data
    public static class SQSLocalProperties {
        private int port;
    }

}
