package com.example.bff;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.bff.domain.repository.RepositoryPackage;
import com.example.bff.infra.common.httpclient.RestTemplateResponseErrorHandler;
import com.example.bff.infra.common.httpclient.WebClientResponseErrorHandler;
import com.example.fw.common.httpclient.RestTemplateLoggingInterceptor;
import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;
import com.example.fw.common.objectstorage.config.S3ConfigPackage;
import com.example.fw.web.token.TransactionTokenPackage;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
@ComponentScan(basePackageClasses = { S3ConfigPackage.class })
//　トランザクショントークンチェックのMyBatisのMapperをスキャンさせるために、業務APのMapper含めて明示的にスキャンする設定を追加
@MapperScan(basePackageClasses = { TransactionTokenPackage.class, RepositoryPackage.class }, annotationClass = Mapper.class)
public class InfraConfig {

    /**
     * WebClientでのログ出力クラス
     */
    @Bean
    public WebClientLoggingFilter webClientLoggingFilter() {
        return new WebClientLoggingFilter();
    }

    /**
     * WebClientでのエラーハンドラークラス
     */
    @Bean
    public WebClientResponseErrorHandler webClientResponseErrorHandler() {
        return new WebClientResponseErrorHandler();
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

    /**
     * RestTemplateの設定
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        // TODO: X-Rayのトレーシング設定
        // ログ出力クラスの設定
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RestTemplateLoggingInterceptor());
        return restTemplateBuilder
                // エラーハンドラークラスの設定
                .errorHandler(new RestTemplateResponseErrorHandler()).interceptors(interceptors).build();
    }

}
