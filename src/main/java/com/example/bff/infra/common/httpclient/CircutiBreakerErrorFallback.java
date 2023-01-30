package com.example.bff.infra.common.httpclient;

import java.util.function.Function;

import com.example.bff.domain.message.MessageIds;
import com.example.fw.common.exception.BusinessException;

import reactor.core.publisher.Mono;

/**
 * CircuitBreakerでのエラー時のデフォルトのフォールバック処理を実装するクラス
 *
 */
public class CircutiBreakerErrorFallback {
    private CircutiBreakerErrorFallback() {
    }

    /**
     * RestTemplateでのデフォルトのフォールバック処理として、業務例外を返却する
     * 
     * @param <T> CircuiteBreakerでRestTemplateがデータ返却する際の型パラメータ
     * @return T
     * @throws BusinessException 業務例外
     */
    public static <T> Function<Throwable, T> throwBusinessException() {
        return throwable -> {
            // RestTemplateResponseErrorHandlerですでに業務例外をスローしていた場合
            if (throwable.getClass().isAssignableFrom(BusinessException.class)) {
                throw (BusinessException) throwable;
            }
            // 業務例外で返却
            throw new BusinessException(throwable, MessageIds.W_EX_8002);
        };
    }

    /**
     * WebClientでのデフォルトのフォールバック処理として、業務例外を返却する
     * @param <T> CircuiteBreakerでWebClientがデータ返却する際の型パラメータ
     * @return 業務例外
     */
    public static <T> Function<Throwable, Mono<T>> returnMonoBusinessException() {
        return throwable -> {
            // WebClientResponseErrorHandlerですでに業務例外をスローしていた場合
            if (throwable.getClass().isAssignableFrom(BusinessException.class)) {
                return Mono.error(throwable);
            }
            return Mono.error(new BusinessException(throwable, MessageIds.W_EX_8002));
        };
    }
}
