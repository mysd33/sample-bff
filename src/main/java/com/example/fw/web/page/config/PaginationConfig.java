package com.example.fw.web.page.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.fw.web.page.PageInfoDialect;

/**
 * ページネーションの設定クラス
 *
 */
@Configuration
@EnableConfigurationProperties({PaginationConfigurationProperties.class})
public class PaginationConfig implements WebMvcConfigurer {
    @Autowired
    private PaginationConfigurationProperties paginationConfigurationProperties;
    
    /**
     * ページネーションの設定
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(paginationConfigurationProperties.getMaxPageSize());
        resolver.setFallbackPageable(PageRequest.of(paginationConfigurationProperties.getDefaultPage(), paginationConfigurationProperties.getDefaultPageSize()));
        resolvers.add(resolver);
    }

    /**
     * ページネーションのページリンクで使用するThymeleafのカスタムDialectの設定
     */
    @Bean
    public PageInfoDialect pageInfoDialect() {
        return new PageInfoDialect();
    }
}
