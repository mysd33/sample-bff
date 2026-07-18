package com.example.fw.common.httpclient.config;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/// RESTクライアント関連の設定クラス
@Configuration
public class WebClientConfig {

    /// WebClientクラス
    @Profile("!xray")
    @Bean
    WebClient webClient(WebClient.Builder builder, WebClientLoggingFilter loggingFilter) {
        return builder.filter(loggingFilter.filter()).build();
    }

    /// WebClientクラス（X-Rayトレーシング SDK）<br>
    ///
    /// @deprecated X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClient webClientWithXRay(WebClient.Builder builder, WebClientLoggingFilter loggingFilter,
        WebClientXrayFilter xrayFilter) {
        return builder.filter(loggingFilter.filter()).filter(xrayFilter.filter()).build();
    }

    /// WebClientでのAWS X-Ray SDKのHttpクライアントトレーシング設定<br>
    ///
    /// @deprecated X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClientXrayFilter webClientXrayFilter() {
        return new WebClientXrayFilter();
    }
}
