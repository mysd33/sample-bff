package com.example.fw.web.token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.terasoluna.gfw.web.mvc.support.CompositeRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;

/**
 * 
 * トランザクショントークンチェックの設定クラス
 *
 */
@Configuration
public class TransactionTokenConfig implements WebMvcConfigurer {
    /**
     * トランザクショントークンの設定
     * 
     */
    @ConfigurationProperties(prefix = "transaction.token")
    @Bean
    public TransactionTokenProperties transactionTokenProperties() {
        return new TransactionTokenProperties();
    }

    /**
     * SpringSecurityのCsrfRequestDataValueProcessorの同名のBean（requestDataValueProcessor）を上書き
     * 
     * application.ymlで、spring.main.allow-bean-definition-overriding=true設定すること
     */
    @Bean
    public RequestDataValueProcessor requestDataValueProcessor() {
        return new CompositeRequestDataValueProcessor(
                // CSRFとTransactionTokenの両方のRequestDataValueProcessorを指定
                new CsrfRequestDataValueProcessor(), new TransactionTokenRequestDataValueProcessor());
    }

    /**
     * 
     * TransactionTokenInterceptorクラスの設定
     * 
     */
    @Bean
    public TransactionTokenInterceptor transactionTokenInterceptor() {
        return new TransactionTokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionTokenInterceptor())
                .addPathPatterns(transactionTokenProperties().getPathPatterns())
                .excludePathPatterns(transactionTokenProperties().getExcludePathPatterns())
                .order(Ordered.LOWEST_PRECEDENCE - 10);
    }

}
