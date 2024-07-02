package com.example.fw.common.httpclient;

import java.net.URLDecoder;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.logging.MonitoringLogger;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.systemdate.SystemDateUtils;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 * WebClient呼び出し時のログを出力する機能</br>
 * TrackIDやREST API呼び出しの処理時間をログに出力するために使用する
 *
 */
@Slf4j
@RequiredArgsConstructor
public class WebClientLoggingFilter {
	private static final String TRACE_ID = "traceId";
	private static final String SPAN_ID = "spanId";
	private static final String UTF_8 = "UTF-8";
	private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
	private static final MonitoringLogger monitoringLogger = LoggerFactory.getMonitoringLogger(log);

	private final Tracer tracer;

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
			} catch (Exception e) {
				// マルチスレッド下の想定外のエラーのため、エラーログを出力
				monitoringLogger.error(CommonFrameworkMessageIds.E_CM_FW_9001, e);
				throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
			}
			// レスポンス受領時にログを出力するため、Tracerから現在のTraceID、SpanIDを取得しておく
			final Span currentSpan = tracer.currentSpan();
			return next.exchange(request).flatMap(response -> {
				// 呼び出し処理実行後、処理時間を計測しログ出力
				long endTime = System.nanoTime();
				double elapsedTime = SystemDateUtils.calcElaspedTimeByMilliSecounds(startTime, endTime);
				try {
					// 別スレッドで動作するため、TraceID、SpanIDをログのMDCに設定
					// 単体テスト実行時等、Spanが存在しない場合があるため、nullチェックを行う
					if (currentSpan != null) {
						String traceId = currentSpan.context().traceId();
						String spanId = currentSpan.context().spanId();
						MDC.put(TRACE_ID, traceId);
						MDC.put(SPAN_ID, spanId);
					}
					appLogger.info(CommonFrameworkMessageIds.I_CM_FW_0002, request.method(),
							URLDecoder.decode(request.url().toASCIIString(), UTF_8), elapsedTime);
				} catch (Exception e) {
					// マルチスレッド下の想定外のエラーのため、エラーログを出力
					monitoringLogger.error(CommonFrameworkMessageIds.E_CM_FW_9001, e);
					throw new SystemException(e, CommonFrameworkMessageIds.E_CM_FW_9001);
				} finally {
					MDC.remove(TRACE_ID);
					MDC.remove(SPAN_ID);
				}
				return Mono.just(response);
			});
		};
	}

}
