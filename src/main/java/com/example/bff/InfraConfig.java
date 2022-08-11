package com.example.bff;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.example.bff.domain.repository.TodoRepository;
import com.example.bff.infra.repository.TodoRepositoryStub;
import com.example.fw.common.httpclient.RestTemplateLoggingInterceptor;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
public class InfraConfig {

	//REST APIでアクセスしない場合のスタブ	
	@Bean
	public TodoRepository todoRepository() {
		return new TodoRepositoryStub();
		//return new TodoRepositoryImpl();
	}
	
	/**
	 * Restクライアントの設定
	 * 
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new RestTemplateLoggingInterceptor());
		return restTemplateBuilder
				.interceptors(interceptors)				
				.build();
	}

}
