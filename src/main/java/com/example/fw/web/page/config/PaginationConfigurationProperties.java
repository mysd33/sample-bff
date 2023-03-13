package com.example.fw.web.page.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("pagination")
public class PaginationConfigurationProperties {
    // 参考
    // https://macchinetta.github.io/server-guideline-thymeleaf/1.8.1.SP1.RELEASE/ja/ArchitectureInDetail/WebApplicationDetail/Pagination.html#pageablehandlermethodargumentresolver
    
    // PageableHandlerMethodArgumentResolver#maxPageSizeプロパティに相当
    // 取得件数として許可する最大値
    private int maxPageSize = 100;
    // PageableHandlerMethodArgumentResolver#fallbackPageableプロパティで指定するページ位置に相当
    // ページ位置未指定時の、デフォルトのページ位置    
    private int defaultPage = 0;
    // PageableHandlerMethodArgumentResolver#fallbackPageableプロパティで指定する取得件数に相当
    // ページ位置未指定時の、デフォルトの取得件数（1ページ当たりの件数）
    private int defaultPageSize = 10;

}
