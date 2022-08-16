package com.example.fw.common.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.FrameworkMessageIds;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * WebClient呼び出し時のログを出力する機能
 * Spring Sleuthが出力するTrackID等をログに出力するために使用する
 *
 */
@Slf4j
public class WebClientLoggingFilter {
	private final static ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
		
	public ExchangeFilterFunction filter() {
		return (request, next) -> {
			try {
				appLogger.info(FrameworkMessageIds.I_FW_0003, request.method(),
						URLDecoder.decode(request.url().toASCIIString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new SystemException(e, FrameworkMessageIds.E_FW_9001);
			}
			return next.exchange(request);
		};
	}
}
