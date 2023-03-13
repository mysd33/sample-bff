package com.example.fw.web.page.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("pagination")
public class PaginationConfigurationProperties {
    // 最大ページ数
    private int maxPageSize = 100;
    // デフォルトページ数
    private int defaultPage = 0;
    // デフォルトの1ページ当たりの件数
    private int defaultPageSize = 5;

}
