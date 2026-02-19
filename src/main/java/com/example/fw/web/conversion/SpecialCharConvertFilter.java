package com.example.fw.web.conversion;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * 特殊文字のコードポイント変換フィルタ
 */
public class SpecialCharConvertFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SpecialCharConvertRequestWrapper wrappedRequest = new SpecialCharConvertRequestWrapper(
                (jakarta.servlet.http.HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }

}
