package com.example.fw.common.async.config;

import com.example.fw.common.constants.FrameworkConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// SQSのプロパティクラス
@Data
@ConfigurationProperties(prefix = SQSCommonConfigurationProperties.PROPERTY_PREFIX)
public class SQSCommonConfigurationProperties {

    // 非同期実行依頼のプロパティのプレフィックス
    static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "sqs";
    // リージョン（デフォルト: ap-northeast-1）
    private String region = "ap-northeast-1";
    // プリフェッチ数
    private int numberOfMessagesToPrefetch = 0;
    // HTTPコネクションプールの最大接続数（AWS SDKのデフォルト値50）
    // https://github.com/aws/aws-sdk-java-v2/blob/master/http-client-spi/src/main/java/software/amazon/awssdk/http/SdkHttpConfigurationOption.java#L151
    private int maxConnections = 50;
    // HTTPコネクション確立時のタイムアウト（ミリ秒。AWS SDKのデフォルト値2秒 = 2000ミリ秒）
    // https://github.com/aws/aws-sdk-java-v2/blob/master/http-client-spi/src/main/java/software/amazon/awssdk/http/SdkHttpConfigurationOption.java#L142
    private int connectionTimeout = 2000;

    // ローカルSQSの設定
    private SQSLocalProperties sqslocal;

    @Data
    public static class SQSLocalProperties {

        private int port;
    }

}
