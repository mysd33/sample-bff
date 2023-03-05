package com.example.fw.common.httpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;

/**
 * RESTクライアント関連の設定クラス 
 *
 */
@Configuration
public class WebClientConfig {
    /**
     * WebClientでのログ出力クラス
     */
    @Bean
    public WebClientLoggingFilter webClientLoggingFilter() {
        return new WebClientLoggingFilter();
    }
    
    /**
     * 
     * WebClientクラス（X-Rayトレーシングあり）
     * 
     */
    @Profile("!xray")
    @Bean
    public WebClient webClientWithoutXRay(WebClientLoggingFilter loggingFilter) {
        return WebClient.builder().filter(loggingFilter.filter()).build();
    }

    /**
     * 
     * WebClientクラス（X-Rayトレーシングあり）
     * 
     */
    @Profile("xray")
    @Bean
    public WebClient webClientWithXRay(WebClientLoggingFilter loggingFilter, WebClientXrayFilter xrayFilter) {
        return WebClient.builder().filter(loggingFilter.filter()).filter(xrayFilter.filter()).build();
    }
}
