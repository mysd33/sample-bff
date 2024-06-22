package com.example.fw.common.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.systemdate.SystemDateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * WebClient呼び出し時のログを出力する機能</br>
 * TrackIDやREST API呼び出しの処理時間をログに出力するために使用する
 *
 */
@Slf4j
public class WebClientLoggingFilter {
	private static final String UTF_8 = "UTF-8";
	private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

	/**
	 * WebClient呼び出し時のログを出力する
	 * 
	 * @return
	 */
	public ExchangeFilterFunction filter() {
		return (request, next) -> {
			// 処理時間を計測しログ出力
			long startTime = System.nanoTime();
			try {
				appLogger.info(CommonFrameworkMessageIds.I_CM_FW_0001, request.method(),
						URLDecoder.decode(request.url().toASCIIString(), UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
			}
			return next.exchange(request).flatMap(response -> {
				// 呼び出し処理実行後、処理時間を計測しログ出力
				long endTime = System.nanoTime();
				double elapsedTime = SystemDateUtils.calcElaspedTimeByMilliSecounds(startTime, endTime);
				try {
					appLogger.info(CommonFrameworkMessageIds.I_CM_FW_0002, request.method(),
							URLDecoder.decode(request.url().toASCIIString(), UTF_8), elapsedTime);
				} catch (UnsupportedEncodingException e) {
					throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
				}
				return Mono.just(response);
			});
		};
	}

}
