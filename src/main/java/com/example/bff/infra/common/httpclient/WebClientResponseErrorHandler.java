package com.example.bff.infra.common.httpclient;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.example.bff.domain.message.MessageIds;
import com.example.bff.infra.common.resource.ErrorResponse;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientResponseErrorHandler {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    /**
     * クライアント起因のエラー（4xx）の場合に例外オブジェクトを作成する
     * 
     * @param response
     * @return
     */
    public Mono<Exception> createClientErrorException(ClientResponse response) {

        try {
            // TODO: この中ではblockメソッドが使えない模様。
            // java.lang.IllegalStateException: block()/blockFirst()/blockLast() are
            // blocking, which is not supported in thread reactor-http-nio-2
            return response.bodyToMono(ErrorResponse.class).flatMap(body -> {
                // サーバ側のErrorResponseに含まれるをもとにエラーメッセージを画面表示する
                String code = body.getCode();
                String message = body.getMessage();
                return Mono.error(new BusinessException(ResultMessage.builder().type(ResultMessageType.WARN)
                        .code(MessageIds.W_EX_8001).args(new String[] { code }).message(message).build()));
            });
        } catch (Exception e) {
            HttpStatusCode statusCode = response.statusCode();
            appLogger.warn(MessageIds.W_EX_8001, e, statusCode.value());
            return Mono.error(new BusinessException(
                    ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8002).build()));
        }
    }

    /**
     * サーバ起因エラー（5xx）の場合に例外オブジェクトを作成する
     */
    public Mono<Exception> createServerErrorException(ClientResponse response) {
        try {
            return response.bodyToMono(ErrorResponse.class).flatMap(body -> {
                String code = body.getCode();
                String message = body.getMessage();
                // サービスから取得したErrorResponseを警告ログ出力し、定型的なメッセージを画面表示する
                String logMessage = new StringBuilder("[").append(code).append("]").append(message).toString();
                appLogger.warn(MessageIds.W_EX_8001, logMessage);
                return Mono.error(new BusinessException(
                        ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8002).build()));
            });
        } catch (Exception e) {
            HttpStatusCode statusCode = response.statusCode();
            appLogger.warn(MessageIds.W_EX_8001, e, statusCode.value());            
            return Mono.error(new BusinessException(
                    ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8002).build()));
        }
    }
}
