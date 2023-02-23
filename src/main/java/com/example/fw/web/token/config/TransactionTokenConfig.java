package com.example.fw.web.token.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.terasoluna.gfw.web.mvc.support.CompositeRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.TokenStringGenerator;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInfoStore;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenStore;

import com.example.fw.web.token.MyBatisTransactionTokenStore;
import com.example.fw.web.token.TransactionTokenCleaningListener;

/**
 * 
 * トランザクショントークンチェックの設定クラス
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "transaction.token", name = "enabled", havingValue = "true", matchIfMissing = true)
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
        return new TransactionTokenInterceptor(new TokenStringGenerator(), new TransactionTokenInfoStore(),
                transactionTokenStore());
    }

    /**
     * TransationTokenStoreクラスの設定（RDBのテーブル管理するTransationTokenStoreに差し替え
     * 
     */
    @Bean
    public TransactionTokenStore transactionTokenStore() {
        return new MyBatisTransactionTokenStore();
    }

    /**
     * セッションタイムアウト等のセッション破棄時にトークンを自動削除するHttpSessionListenerの設定
     * 
     */
    @Bean
    public TransactionTokenCleaningListener transactionTokenCleaningListener() {
        return new TransactionTokenCleaningListener();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionTokenInterceptor())
                .addPathPatterns(transactionTokenProperties().getPathPatterns())
                .excludePathPatterns(transactionTokenProperties().getExcludePathPatterns())
                .order(Ordered.LOWEST_PRECEDENCE - 10);
    }

}