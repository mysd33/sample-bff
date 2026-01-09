package com.example.fw.common.reports.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 帳票出力機能の機能設定クラス
 */
@Configuration
@EnableConfigurationProperties(ReportsConfigurationProperties.class)
public class ReportsConfig {

}
