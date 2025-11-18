package com.example.fw.common.httpclient;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
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

import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;
import com.example.fw.common.systemdate.SystemDateUtils;

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
    private static final String USER_ID = "userId";
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    /**
     * WebClient呼び出し時のログを出力する
     * 
     */
    public ExchangeFilterFunction filter() {
        return (request, next) -> {
            final ClientRequest clientRequest;
            // 処理時間を計測しログ出力
            long startTime = System.nanoTime();
            appLogger.info(CommonFrameworkMessageIds.I_FW_HTTP_0001, request.method(),
                    URLDecoder.decode(request.url().toASCIIString(), StandardCharsets.UTF_8));
            // 電文ログのデバッグログ出力対応したClientRequestを作成
            String currentTraceId = MDC.get(TRACE_ID);
            String currentSpanId = MDC.get(SPAN_ID);
            String userId = MDC.get(USER_ID);
            clientRequest = createClientRequestForDebugLog(request, new MDCData(currentTraceId, currentSpanId, userId));
            return next.exchange(clientRequest).flatMap(response -> {
                // 呼び出し処理実行後、処理時間を計測しログ出力
                long endTime = System.nanoTime();
                double elapsedTime = SystemDateUtils.calcElapsedTimeByMilliSeconds(startTime, endTime);
                appLogger.info(CommonFrameworkMessageIds.I_FW_HTTP_0002, request.method(),
                        URLDecoder.decode(request.url().toASCIIString(), StandardCharsets.UTF_8), elapsedTime);
                return createResponseMonoForDebugLog(response);
            });
        };
    }

    /**
     * リクエストデータの電文ログをデバッグログ出力するためのClientRequest作成</br>
     * 
     * この時点で、MDCに設定したデータが引き継がれていないので、引き継ぐためにパラメータで渡す
     * 
     * @param request リクエストデータ
     * @return 電文ログ出力対応したリクエストデータ
     */
    private ClientRequest createClientRequestForDebugLog(final ClientRequest request, MDCData mdcData) {
        final ClientRequest clientRequest;
        if (appLogger.isDebugEnabled()) {
            // リクエストデータの電文ログをデバッグログ出力するよう設定
            // https://ik.am/entries/632
            // https://stackoverflow.com/questions/67300470/webclient-request-and-response-body-logging
            BodyInserter<?, ? super ClientHttpRequest> bodyInserter = request.body();
            clientRequest = ClientRequest.from(request).body((outputMessage, context) -> bodyInserter
                    .insert(new LoggingClientHttpRequest(outputMessage, mdcData), context)).build();
        } else {
            clientRequest = request;
        }
        return clientRequest;
    }

    /**
     * レスポンスデータの電文ログをデバッグログ出力するためのMono<ClientResponse>を作成する
     * 
     * @param response レスポンスデータ
     */
    private Mono<? extends ClientResponse> createResponseMonoForDebugLog(final ClientResponse response) {
        // レスポンスデータのログを出力する
        // https://stackoverflow.com/questions/73299170/how-to-log-response-body-from-client-by-overriding-exchangefilterfunction-ofresp
        // https://stackoverflow.com/questions/67300470/webclient-request-and-response-body-logging
        return appLogger.isDebugEnabled() ? //
                Mono.just(response.mutate().body(data -> data.map(dataBuffer -> {
                    appLogger.debug("レスポンスデータ: {}", dataBuffer.toString(StandardCharsets.UTF_8));
                    return dataBuffer;
                })).build()) //
                : Mono.just(response);
    }

    /**
     * リクエストデータをデバッグログに出力するためのClientHttpRequest実装クラス
     */
    @RequiredArgsConstructor
    class LoggingClientHttpRequest implements ClientHttpRequest {
        private final ClientHttpRequest delegateRequest;
        private final MDCData mdcData;

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
            return appLogger.isDebugEnabled() ? delegateRequest.writeWith(DataBufferUtils.join(body).doOnNext(data ->
            // 別スレッドで動作するため、TraceID、SpanIDをMDCに設定してログ出力
            new MDCScope(mdcData).execute(data,
                    d -> appLogger.debug("リクエストデータ: {}", d.toString(StandardCharsets.UTF_8)))))
                    // デバッグレベルじゃない場合は通常の処理
                    : this.delegateRequest.writeWith(body);
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return appLogger.isDebugEnabled() ? delegateRequest
                    .writeAndFlushWith(Flux.from(body).map(b -> DataBufferUtils.join(b).doOnNext(data ->
                    // 別スレッドで動作するため、TraceID、SpanIDをMDCに設定してログ出力
                    new MDCScope(mdcData).execute(data,
                            d -> appLogger.debug("リクエストデータ: {}", d.toString(StandardCharsets.UTF_8))))))
                    // デバッグレベルじゃない場合は通常の処理
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

        @Override
        public Map<String, Object> getAttributes() {
            return delegateRequest.getAttributes();
        }

    }

    /**
     * MDCに設定するデータを保持するクラス
     */
    @RequiredArgsConstructor
    class MDCData {
        private final String traceId;
        private final String spanId;
        private final String userId;

        void putToMDC() {
            if (traceId != null && spanId != null) {
                MDC.put(TRACE_ID, traceId);
                MDC.put(SPAN_ID, spanId);
            }
            if (userId != null) {
                MDC.put(USER_ID, userId);
            }
        }

        void removeFromMDC() {
            MDC.remove(TRACE_ID);
            MDC.remove(SPAN_ID);
            MDC.remove(USER_ID);
        }
    }

    /**
     * MDCの設定スコープを実現するクラス
     */
    @RequiredArgsConstructor
    class MDCScope {
        private final MDCData mdcData;

        <T> void execute(T data, Consumer<T> consumer) {
            try {
                // 単体テスト実行時等、Spanが存在しない場合があるため、nullチェックを行ってからMDCに設定
                mdcData.putToMDC();
                consumer.accept(data);
            } finally {
                mdcData.removeFromMDC();
            }
        }
    }

}
