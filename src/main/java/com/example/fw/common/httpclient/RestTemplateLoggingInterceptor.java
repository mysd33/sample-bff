package com.example.fw.common.httpclient;

import static com.example.fw.common.message.FrameworkMessageIds.*;
import java.io.IOException;
import java.net.URLDecoder;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestTemplate呼び出し時のログを出力する機能
 * Spring Sleuthが出力するTrackID等をログに出力するために使用する
 *
 */
@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
	private final static ApplicationLogger appLoger = LoggerFactory.getApplicationLogger(log);
	
	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
			throws IOException {
		appLoger.info(I_FW_0003, request.getMethod(), URLDecoder.decode(request.getURI().toASCIIString(), "UTF-8"));
		return execution.execute(request, body);
	}

}
