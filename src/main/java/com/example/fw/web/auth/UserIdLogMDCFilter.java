package com.example.fw.web.auth;

import java.io.IOException;

import org.jboss.logging.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * ログにMDCでユーザIDを出力するためのFilter
 */
@RequiredArgsConstructor
public class UserIdLogMDCFilter extends OncePerRequestFilter {
    private final UserNameProvider userNameProvider;
    private static final String MDC_KEY_USER_ID = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // ログイン済みユーザIDをログ出力するために取得
        String userName = userNameProvider != null ? userNameProvider.getUserName() : null;
        try {
            MDC.put(MDC_KEY_USER_ID, userName);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY_USER_ID);
        }
    }

}
