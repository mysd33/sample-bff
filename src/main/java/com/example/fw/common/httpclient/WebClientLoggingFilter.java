package com.example.fw.common.httpclient;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
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
import reactor.core.publisher.Flux;
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
			final ClientRequest clientRequest;
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

			// 電文ログのデバッグログ出力対応したClientRequestを作成
			clientRequest = createClientRequestForDebugLog(request, currentSpan);

			return next.exchange(clientRequest).flatMap(response -> {
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

				return createResponseMonoForDebugLog(response, currentSpan);
			});
		};
	}

	/**
	 * レスポンスデータの電文ログをデバッグログ出力するためのMono<ClientResponse>を作成する
	 * @param response レスポンスデータ
	 * @param currentSpan 現在のSpan
	 * @return　電文ログ出力対応したレスポンスデータ 
	 */
	private Mono<? extends ClientResponse> createResponseMonoForDebugLog(ClientResponse response, final Span currentSpan) {
		// レスポンスデータのログを出力する		
		// https://stackoverflow.com/questions/73299170/how-to-log-response-body-from-client-by-overriding-exchangefilterfunction-ofresp
		// https://stackoverflow.com/questions/67300470/webclient-request-and-response-body-logging
		return appLogger.isDebugEnabled() ? Mono.just(response.mutate().body(data -> data.map(dataBuffer -> {
			try {
				// 別スレッドで動作するため、TraceID、SpanIDをログのMDCに設定
				// 単体テスト実行時等、Spanが存在しない場合があるため、nullチェックを行う
				if (currentSpan != null) {
					String traceId = currentSpan.context().traceId();
					String spanId = currentSpan.context().spanId();
					MDC.put(TRACE_ID, traceId);
					MDC.put(SPAN_ID, spanId);
				}
				appLogger.debug("レスポンスデータ: {}", dataBuffer.toString(StandardCharsets.UTF_8));
				return dataBuffer;
			} finally {
				MDC.remove(TRACE_ID);
				MDC.remove(SPAN_ID);
			}
		})).build()) : Mono.just(response);
	}

	/**
	 * リクエストデータの電文ログをデバッグログ出力するためのClientRequest作成
	 * @param request リクエストデータ
	 * @param currentSpan 現在のSpan
	 * @return 電文ログ出力対応したリクエストデータ
	 */
	private ClientRequest createClientRequestForDebugLog(final ClientRequest request, final Span currentSpan) {
		final ClientRequest clientRequest;
		if (appLogger.isDebugEnabled()) {
			// リクエストデータの電文ログをデバッグログ出力するよう設定
			// https://ik.am/entries/632
			// https://stackoverflow.com/questions/67300470/webclient-request-and-response-body-logging
			BodyInserter<?, ? super ClientHttpRequest> bodyInserter = request.body();
			clientRequest = ClientRequest.from(request).body((outputMessage, context) -> bodyInserter
					.insert(new LoggingClientHttpRequest(outputMessage, currentSpan), context)).build();
		} else {
			clientRequest = request;
		}
		return clientRequest;
	}

	/**
	 * リクエストデータをデバッグログに出力するためのClientHttpRequest実装クラス
	 */
	@RequiredArgsConstructor
	class LoggingClientHttpRequest implements ClientHttpRequest {
		private final ClientHttpRequest delegateRequest;
		private final Span currentSpan;

		@Override
		public DataBufferFactory bufferFactory() {
			return delegateRequest.bufferFactory();
		}

		@Override
		public void beforeCommit(Supplier<? extends Mono<Void>> action) {
			delegateRequest.beforeCommit(action);
		}

		@Override
		public boolean isCommitted() {
			return delegateRequest.isCommitted();
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			return appLogger.isDebugEnabled() ? delegateRequest.writeWith(DataBufferUtils.join(body).doOnNext(data -> {
				try {
					if (currentSpan != null) {
						String traceId = currentSpan.context().traceId();
						String spanId = currentSpan.context().spanId();
						MDC.put(TRACE_ID, traceId);
						MDC.put(SPAN_ID, spanId);
					}
					appLogger.debug("リクエストデータ: {}", data.toString(StandardCharsets.UTF_8));
				} finally {
					MDC.remove(TRACE_ID);
					MDC.remove(SPAN_ID);
				}
			})) : this.delegateRequest.writeWith(body);
		}

		@Override
		public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
			return appLogger.isDebugEnabled()
					? delegateRequest.writeAndFlushWith(Flux.from(body).map(b -> DataBufferUtils.join(b).doOnNext(data -> {
						try {
							if (currentSpan != null) {
								String traceId = currentSpan.context().traceId();
								String spanId = currentSpan.context().spanId();
								MDC.put(TRACE_ID, traceId);
								MDC.put(SPAN_ID, spanId);
							}
							appLogger.debug("RequestData: {}", data.toString(StandardCharsets.UTF_8));
						} finally {
							MDC.remove(TRACE_ID);
							MDC.remove(SPAN_ID);
						}
					})))
					: this.delegateRequest.writeAndFlushWith(body);
		}

		@Override
		public Mono<Void> setComplete() {
			return delegateRequest.setComplete();
		}

		@Override
		public HttpHeaders getHeaders() {
			return delegateRequest.getHeaders();
		}

		@Override
		public HttpMethod getMethod() {
			return delegateRequest.getMethod();
		}

		@Override
		public URI getURI() {
			return delegateRequest.getURI();
		}

		@Override
		public MultiValueMap<String, HttpCookie> getCookies() {
			return delegateRequest.getCookies();
		}

		@Override
		public <T> T getNativeRequest() {
			return delegateRequest.getNativeRequest();
		}

	}

}
