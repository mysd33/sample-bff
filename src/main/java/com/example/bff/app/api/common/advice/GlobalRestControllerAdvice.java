package com.example.bff.app.api.common.advice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.bff.app.api.APIPackage;
import com.example.bff.infra.common.resource.ErrorResponse;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.exception.SystemException;

import lombok.RequiredArgsConstructor;

/**
 * 
 * 集約例外ハンドリングのためのRestControllerAdviceクラス
 *
 */
@RestControllerAdvice(basePackageClasses = { APIPackage.class })
@RequiredArgsConstructor
public class GlobalRestControllerAdvice extends ResponseEntityExceptionHandler {
    private final ErrorResponseCreator errorResponseCreator;

    /**
     * 入力エラーのハンドリング （MethodArgumentNotValidException）
     * リクエストBODYに指定されたJSONに対する入力チェックでエラーが発生した場合
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return handleBindingResult(ex, ex.getBindingResult(), headers, statusCode, request);
    }
    
    /**
     * 入力エラーのハンドリング （HttpMessageNotReadableException）
     * JSONからResourceオブジェクトを生成する際にエラーが発生した場合
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (ex.getCause() instanceof Exception cause) {
            return handleExceptionInternal(cause, null, headers, statusCode, request);
        } else {
            return handleExceptionInternal(ex, null, headers, statusCode, request);
        }
    }

    private ResponseEntity<Object> handleBindingResult(Exception ex, BindingResult bindingResult, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        ErrorResponse body = errorResponseCreator.createInputErrorResponse(bindingResult, request);

        return handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    /**
     * 業務エラーのハンドリング
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> bussinessExceptionHandler(final BusinessException e, final WebRequest request) {
        ErrorResponse body = errorResponseCreator.createGeneralErrorResponse(e, request);
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * システムエラーのハンドリング
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Object> systemExceptionHandler(final SystemException e, final WebRequest request) {
        ErrorResponse body = errorResponseCreator.createGeneralErrorResponse(e, request);
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * システムエラー（予期せぬ例外）のハンドリング
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exceptionHandler(final Exception e, final WebRequest request) {
        ErrorResponse body = errorResponseCreator.createUnknownErrorResponse(e, request);
        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
