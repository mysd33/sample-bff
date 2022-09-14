package com.example.bff.common.httpclient;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.example.bff.common.resource.ErrorResponse;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.FrameworkMessageIds;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientResponseErrorHandler {
	private final static ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

	/**
	 * クライアント起因のエラー（4xx）の場合に例外オブジェクトを作成する
	 * @param response
	 * @return
	 */
	public Mono<Exception> createClientErrorException(ClientResponse response) {
		
		try {
			//TODO: この中ではblockメソッドが使えない模様。
			// java.lang.IllegalStateException: block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-2
			return response.bodyToMono(ErrorResponse.class)
					.flatMap(body -> {
						// サーバ側のErrorResponseに含まれるをもとにエラーメッセージを画面表示する
						String code = body.getCode();
						String message = body.getMessage();
						return Mono.error(new BusinessException(
								ResultMessage.builder().type(ResultMessageType.WARN).code(FrameworkMessageIds.W_FW_8001)
									.args(new Object[]  { code })
									.message(message).build()));						
					});			
		} catch (Exception e) {			
			HttpStatus statusCode = response.statusCode();
			// TODO: ErrorResponseに変換できない場合のエラーメッセージ
			String logMessage = statusCode.getReasonPhrase();
			appLogger.warn(FrameworkMessageIds.W_FW_8001, logMessage);
			return Mono.error(new BusinessException(
					ResultMessage.builder().type(ResultMessageType.WARN).code(FrameworkMessageIds.W_FW_8002).build()));
		}
	}
	
	/**
	 * サーバ起因エラー（5xx）の場合に例外オブジェクトを作成する
	 */
	public Mono<Exception> createServerErrorException(ClientResponse response) {
		try {
			return  response.bodyToMono(ErrorResponse.class)
					.flatMap(body -> {
						String code = body.getCode();
						String message = body.getMessage();
						// サービスから取得したErrorResponseを警告ログ出力し、定型的なメッセージを画面表示する
						String logMessage = new StringBuilder("[").append(code).append("]").append(message).toString();
						appLogger.warn(FrameworkMessageIds.W_FW_8001, logMessage);
						return Mono.error(new BusinessException(
								ResultMessage.builder().type(ResultMessageType.WARN).code(FrameworkMessageIds.W_FW_8002).build()));
					});					
		} catch (Exception e) {
			HttpStatus statusCode = response.statusCode();
			// TODO: ErrorResponseに変換できない場合のエラーメッセージ
			String logMessage = statusCode.getReasonPhrase();
			appLogger.warn(FrameworkMessageIds.W_FW_8001, logMessage);
			return Mono.error(new BusinessException(
					ResultMessage.builder().type(ResultMessageType.WARN).code(FrameworkMessageIds.W_FW_8002).build()));
		}
	}
}
