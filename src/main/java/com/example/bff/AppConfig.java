package com.example.bff;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.example.bff.domain.message.MessageIds;
import com.example.fw.common.systemdate.SystemDate;
import com.example.fw.common.systemdate.config.SystemDateConfig;
import com.example.fw.web.advice.DefaultErrorResponseCreator;
import com.example.fw.web.advice.ErrorResponseCreator;
import com.example.fw.web.aspect.LogAspect;
import com.example.fw.web.page.config.PaginationConfigPackage;
import com.example.fw.web.servlet.config.TomcatAccessLogConfig;
import com.example.fw.web.token.config.TransactionTokenConfigPackage;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * 
 * アプリケーション層の設定クラス
 *
 */
@Configuration
// ページネーション機能、トランザクショントークンチェック機能の追加
@ComponentScan(basePackageClasses = { PaginationConfigPackage.class, TransactionTokenConfigPackage.class })
// システム日時機能の追加、Tomcatのログ設定の追加
@Import({ SystemDateConfig.class, TomcatAccessLogConfig.class })
public class AppConfig {

    /**
     * エラーレスポンス作成クラス
     */
    @Bean
    ErrorResponseCreator errorResponseCreator(MessageSource messageSource) {
        // オプション引数未指定の場合の例
        // return new DefaultErrorResponseCreator(messageSource, MessageIds.W_EX_2001,
        // MessageIds.E_EX_9001);
        return new DefaultErrorResponseCreator(messageSource, MessageIds.W_EX_2001, MessageIds.E_EX_9001,
                MessageIds.W_EX_2002);
    }

    /**
     * ロギング機能
     */
    @Bean
    LogAspect logAspect(SystemDate systemDate, MessageSource messageSource) {
        return new LogAspect(systemDate, messageSource, MessageIds.W_EX_2001, MessageIds.E_EX_9001);
    }

    /**
     * Springdoc-openapiでスネークケースの設定が反映されるようにするための回避策
     */
    @Bean
    ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }

    /**
     * Springdoc-openapiの定義
     */
    @Bean
    OpenAPI springDocOpenAPI() {
        return new OpenAPI().info(new Info().title("非同期実行APIドキュメント").description("非同期実行管理のためのAPIです。").version("v1.0"));
    }

}
