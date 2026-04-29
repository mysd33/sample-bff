package com.example.fw.common.httpclient.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Hooks;

/**
 * RESTクライアント関連の設定クラス
 *
 */
@Configuration
public class WebClientConfig {
    /**
     * MDC を Reactor Context に伝搬させる設定
     */
    @PostConstruct
    public void setup() {
        ContextRegistry.getInstance().registerThreadLocalAccessor("mdc", //
                MDC::getCopyOfContextMap, //
                ctx -> {
                    if (ctx != null) {
                        MDC.setContextMap(ctx);
                    }
                }, //
                MDC::clear);
        // Reactor で自動伝搬を有効化
        Hooks.enableAutomaticContextPropagation();
    }

    /**
     * WebClientでのログ出力クラス
     */
    @Bean
    WebClientLoggingFilter webClientLoggingFilter() {
        return new WebClientLoggingFilter();
    }

    /**
     * 
     * WebClientクラス
     * 
     */
    @Profile("!xray")
    @Bean
    WebClient webClient(WebClient.Builder builder, WebClientLoggingFilter loggingFilter) {
        return builder.filter(loggingFilter.filter()).build();
    }

    /**
     * 
     * WebClientクラス（X-Rayトレーシング SDK）<br>
     * 
     * X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
     */
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClient webClientWithXRay(WebClient.Builder builder, WebClientLoggingFilter loggingFilter,
            WebClientXrayFilter xrayFilter) {
        return builder.filter(loggingFilter.filter()).filter(xrayFilter.filter()).build();
    }

    /**
     * WebClientでのAWS X-Ray SDKのHttpクライアントトレーシング設定<br>
     * 
     * X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
     */
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClientXrayFilter webClientXrayFilter() {
        return new WebClientXrayFilter();
    }
}
