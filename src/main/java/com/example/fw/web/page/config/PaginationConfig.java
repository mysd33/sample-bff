package com.example.fw.web.page.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.fw.web.page.PageInfoDialect;

/**
 * ページネーションの設定クラス
 *
 */
@Configuration
public class PaginationConfig {
    /**
     * ページネーションのページリンクで使用するThymeleafのカスタムDialectの設定
     */
    @Bean
    public PageInfoDialect pageInfoDialect() {
        return new PageInfoDialect();
    }
}
