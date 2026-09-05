package com.example.fw.web.conversion.form;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/// 特殊文字のコードポイント変換フィルタ
public class SpecialCharConvertFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        var wrappedRequest = new SpecialCharConvertRequestWrapper(
            (jakarta.servlet.http.HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }

}
