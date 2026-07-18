package com.example.fw.common.httpclient.config;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class CommonWebClientConfig {

    /// MDC を Reactor Context に伝搬させる設定
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

    /// WebClientでのログ出力クラス
    @Bean
    WebClientLoggingFilter webClientLoggingFilter() {
        return new WebClientLoggingFilter();
    }
}
