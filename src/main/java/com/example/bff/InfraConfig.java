package com.example.bff;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.example.bff.common.httpclient.RestTemplateResponseErrorHandler;
import com.example.bff.common.httpclient.WebClientResponseErrorHandler;
import com.example.fw.common.httpclient.RestTemplateLoggingInterceptor;
import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
public class InfraConfig {
	/**
	 * WebClientでのログ出力クラス
	 */
	@Bean 
	public WebClientLoggingFilter webClientLoggingFilter() {
		return new WebClientLoggingFilter();
	}

	/**
	 * WebClientでのX-Rayクラス
	 * 
	 */
	@Bean
	public WebClientXrayFilter webClientXrayFilter() {
		return new WebClientXrayFilter();
	}

	
	/**
	 * WebClientでのエラーハンドラークラス
	 */
	@Bean 
	public WebClientResponseErrorHandler webClientResponseErrorHandler() {
		return new WebClientResponseErrorHandler();
	}
	
	
	/**
	 * RestTemplateの設定
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		// ログ出力クラスの設定
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new RestTemplateLoggingInterceptor());
		return restTemplateBuilder
				// エラーハンドラークラスの設定
				.errorHandler(new RestTemplateResponseErrorHandler()).interceptors(interceptors).build();
	}

}
