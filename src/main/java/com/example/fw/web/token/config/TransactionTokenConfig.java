package com.example.fw.web.token.config;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor;
import org.springframework.session.web.http.SessionEventHttpSessionListenerAdapter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.terasoluna.gfw.web.mvc.support.CompositeRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.TokenStringGenerator;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInfoStore;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenStore;

import com.example.fw.web.token.TransactionTokenCleaningListener;

import jakarta.servlet.http.HttpSessionListener;

/**
 * 
 * トランザクショントークンチェックの設定クラス
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "transaction-token", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({TransactionTokenConfigurationProperties.class})
public class TransactionTokenConfig implements WebMvcConfigurer {
    @Autowired
    private TransactionTokenStore transactionTokenStore;
    
    @Autowired
    private TransactionTokenConfigurationProperties transactionTokenProperties;

    /**
     * SpringSecurityのCsrfRequestDataValueProcessorの同名のBean（requestDataValueProcessor）定義を、
     * CompolistRequestDataValueProcessorによるBean定義に上書き
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor requestDataValueProcessorPostProcessor() {
        return new BeanDefinitionRegistryPostProcessor() {

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                // SpringSecurityのCsrfRequestDataValueProcessorの同名のBean（requestDataValueProcessor）を上書き
                ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
                RequestDataValueProcessor[] requestDataValueProcessors = new RequestDataValueProcessor[] {
                        new CsrfRequestDataValueProcessor(),
                        new TransactionTokenRequestDataValueProcessor()
                        //new TraceableTransactionTokenRequestDataValueProcessor() 
                        };
                constructorArgumentValues.addIndexedArgumentValue(0, requestDataValueProcessors);                
                RootBeanDefinition rootBean = new RootBeanDefinition(CompositeRequestDataValueProcessor.class,
                        constructorArgumentValues, null);
                registry.removeBeanDefinition("requestDataValueProcessor");
                registry.registerBeanDefinition("requestDataValueProcessor", rootBean);
            }
        };
    }

    /**
     * 
     * TransactionTokenInterceptorクラスの設定
     * 
     */
    @Bean
    public TransactionTokenInterceptor transactionTokenInterceptor() {
        return new TransactionTokenInterceptor(new TokenStringGenerator(), new TransactionTokenInfoStore(),
                transactionTokenStore);
    }

    /**
     * セッションタイムアウト時にHttpSessionListener（TransactionTokenCleaningListener）を動作させるための設定
     */
    @Bean
    //Spring Session with Redisがある場合はBean定義不要
    @ConditionalOnMissingClass("org.springframework.session.data.redis.RedisSessionRepository")
    public SessionEventHttpSessionListenerAdapter sessionEventHttpSessionListenerAdapter(
            List<HttpSessionListener> listeners) {
        return new SessionEventHttpSessionListenerAdapter(listeners);
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
                .addPathPatterns(transactionTokenProperties.getPathPatterns())
                .excludePathPatterns(transactionTokenProperties.getExcludePathPatterns())
                .order(Ordered.LOWEST_PRECEDENCE - 10);
    }

}