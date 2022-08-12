package com.example.bff;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.example.fw.common.httpclient.RestTemplateLoggingInterceptor;
import com.example.fw.common.httpclient.RestTemplateResponseErrorHandler;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
public class InfraConfig {

	/**
	 * Restクライアントの設定
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		//ログの設定
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new RestTemplateLoggingInterceptor());
		return restTemplateBuilder
				//エラーハンドラーの設定
				.errorHandler(new RestTemplateResponseErrorHandler())
				.interceptors(interceptors)				
				.build();
	}

}
