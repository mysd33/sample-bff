package com.example.fw.web.token.config;

import java.util.Collections;
import java.util.List;

import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;

/**
 * 
 * トランザクショントークンチェックのプロパティクラス
 *
 */
public class TransactionTokenProperties {
    /**
     * {@link TransactionTokenInterceptor}を適用するパスのパターン。
     * @see InterceptorRegistration#addPathPatterns(String...)
     */
    private List<String> pathPatterns = Collections.emptyList();

    /**
     * {@link TransactionTokenInterceptor}の適用を除外するパスのパターン。
     * @see InterceptorRegistration#excludePathPatterns(String...)
     */
    private List<String> excludePathPatterns = Collections.emptyList();

    public List<String> getPathPatterns() {
        return pathPatterns;
    }

    public void setPathPatterns(List<String> pathPatterns) {
        this.pathPatterns = pathPatterns;
    }

    public List<String> getExcludePathPatterns() {
        return excludePathPatterns;
    }

    public void setExcludePathPatterns(List<String> excludePathPatterns) {
        this.excludePathPatterns = excludePathPatterns;
    }

}