package com.example.bff.infra.common.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.ResponseErrorHandler;

import com.example.bff.domain.message.MessageIds;
import com.example.bff.infra.common.resource.ErrorResponse;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * REST API呼び出し時のエラーハンドラクラス
 * 
 */
@Slf4j
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    // TODO: DI
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return (httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError());
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpStatusCode httpStatusCode = response.getStatusCode();
        String statusText = response.getStatusText();
        byte[] responseBody = getResponseBody(response);
        Charset charset = getCharset(response);
        ErrorResponse errorResponse = null;
        String code = null;
        String message = null;
        if (httpStatusCode.is4xxClientError()) {
            try {
                errorResponse = mapper.readValue(responseBody, ErrorResponse.class);
            } catch (Exception e) {
                // ErrorResponseに変換できない場合
                throwBussinessExceptionForUnknownErrorResponse(httpStatusCode, statusText, responseBody, charset);
            }
            // サーバ側のErrorResponseに含まれるメッセージをもとにエラーメッセージを画面表示する
            message = errorResponse.getMessage();
            throw new BusinessException(ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8001)
                    .message(message).build());
        } else if (httpStatusCode.is5xxServerError()) {
            try {
                errorResponse = mapper.readValue(responseBody, ErrorResponse.class);
            } catch (Exception e) {
                // ErrorResponseに変換できない場合
                throwBussinessExceptionForUnknownErrorResponse(httpStatusCode, statusText, responseBody, charset);
            }
            code = errorResponse.getCode();
            message = errorResponse.getMessage();
            // サービスから取得したErrorResponseを警告ログ出力し、定型的なメッセージを画面表示する
            String logMessage = new StringBuilder("[").append(code).append("]").append(message).toString();
            appLogger.warn(MessageIds.W_EX_8001, logMessage);
            throw new BusinessException(
                    ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8002).build());
        } else {
            throwBussinessExceptionForUnknownErrorResponse(httpStatusCode, statusText, responseBody, charset);
        }
    }

    /**
     * 不明なエラーの場合は、サービスから取得したエラー情報を警告ログ出力し、定型的なメッセージを画面表示する
     * 
     * @param httpStatusCode
     * @param statusText
     * @param responseBody
     * @param charset
     */
    private void throwBussinessExceptionForUnknownErrorResponse(HttpStatusCode httpStatusCode, String statusText,
            byte[] responseBody, Charset charset) {
        String logMessage = getErrorMessage(httpStatusCode.value(), statusText, responseBody, charset);
        appLogger.warn(MessageIds.W_EX_8001, logMessage);
        throw new BusinessException(
                ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8002).build());
    }

    protected byte[] getResponseBody(ClientHttpResponse response) {
        try {
            return FileCopyUtils.copyToByteArray(response.getBody());
        } catch (IOException ex) {
            // ignore
        }
        return new byte[0];
    }

    protected Charset getCharset(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        return (contentType != null ? contentType.getCharset() : null);
    }

    private String getErrorMessage(int rawStatusCode, String statusText, @Nullable byte[] responseBody,
            @Nullable Charset charset) {

        String preface = rawStatusCode + " " + statusText + ": ";
        if (ObjectUtils.isEmpty(responseBody)) {
            return preface + "[no body]";
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        int maxChars = 200;

        if (responseBody.length < maxChars * 2) {
            return preface + "[" + new String(responseBody, charset) + "]";
        }

        try {
            Reader reader = new InputStreamReader(new ByteArrayInputStream(responseBody), charset);
            CharBuffer buffer = CharBuffer.allocate(maxChars);
            reader.read(buffer);
            reader.close();
            buffer.flip();
            return preface + "[" + buffer.toString() + "... (" + responseBody.length + " bytes)]";
        } catch (IOException ex) {
            // should never happen
            throw new IllegalStateException(ex);
        }
    }
}
