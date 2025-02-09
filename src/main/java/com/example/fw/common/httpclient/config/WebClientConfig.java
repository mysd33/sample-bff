package com.example.fw.common.httpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;

import io.micrometer.tracing.Tracer;

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
    WebClientLoggingFilter webClientLoggingFilter(Tracer tracer) {
        return new WebClientLoggingFilter(tracer);
    }
    
    /**
     * 
     * WebClientクラス（X-Rayトレーシングあり）
     * 
     */
    @Profile("!xray")
    @Bean
    WebClient webClientWithoutXRay(WebClient.Builder builder, WebClientLoggingFilter loggingFilter) {
        return builder.filter(loggingFilter.filter()).build();
    }

    /**
     * 
     * WebClientクラス（X-Rayトレーシングあり）
     * 
     */
    @Profile("xray")
    @Bean
    WebClient webClientWithXRay(WebClient.Builder builder, WebClientLoggingFilter loggingFilter, WebClientXrayFilter xrayFilter) {
        return builder.filter(loggingFilter.filter()).filter(xrayFilter.filter()).build();
    }
    
    /**
     * WebClientでのAWS X-RayのHttpクライアントトレーシング設定
     * 
     */
    @Profile("xray")
    @Bean
    WebClientXrayFilter webClientXrayFilter() {
        return new WebClientXrayFilter();
    }
}
