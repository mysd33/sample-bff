package com.example.bff;

import com.example.bff.domain.message.MessageIds;
import com.example.fw.common.systemdate.SystemDate;
import com.example.fw.common.systemdate.config.SystemDateConfigPackage;
import com.example.fw.web.advice.DefaultErrorResponseCreator;
import com.example.fw.web.advice.ErrorResponseCreator;
import com.example.fw.web.aspect.LogAspect;
import com.example.fw.web.conversion.form.config.FormConversionConfigPackage;
import com.example.fw.web.page.config.PaginationConfigPackage;
import com.example.fw.web.token.config.TransactionTokenConfigPackage;
import com.example.fw.web.tomcat.config.TomcatAccessLogConfigPackage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//springdoc-openapiの内部io.swagger.v3.core.jacksonはJackson2を使用しているため
//Jackson2のObjectMapperをインポートする


/// アプリケーション層の設定クラス
@Configuration
// ページネーション機能、トランザクショントークンチェック機能、システム日時機能、
// Tomcatアクセスログ設定、Formの特殊文字のコードポイント変換機能の追加
@ComponentScan(basePackageClasses = {PaginationConfigPackage.class,
    TransactionTokenConfigPackage.class, SystemDateConfigPackage.class,
    TomcatAccessLogConfigPackage.class,
    FormConversionConfigPackage.class})
public class AppConfig {

    /// エラーレスポンス作成クラス
    @Bean
    ErrorResponseCreator errorResponseCreator(MessageSource messageSource) {
        // オプション引数未指定の場合の例
        // return new DefaultErrorResponseCreator(messageSource, MessageIds.W_EX_2001,
        // MessageIds.E_EX_9001);

        // オプション引数を指定した場合の例
        // return new DefaultErrorResponseCreator(messageSource, MessageIds.W_EX_2001,
        // MessageIds.E_EX_9001,
        // MessageIds.W_EX_2002);

        // Builderパターンを使用した場合の記載例
        return DefaultErrorResponseCreator.builder().messageSource(messageSource)
            .inputErrorMessageId(MessageIds.W_EX_2001)//
            .unexpectedErrorMessageId(MessageIds.E_EX_9001)//
            .requestBodyValidationErrorMessageId(MessageIds.W_EX_2002)//
            .build();
    }

    /// ロギング機能
    @Bean
    LogAspect logAspect(SystemDate systemDate, MessageSource messageSource) {
        return new LogAspect(systemDate, messageSource, MessageIds.W_EX_2001, MessageIds.E_EX_9001);
    }

    /// Springdoc-openapiでスネークケースの設定が反映されるようにするための回避策
    @Bean
    ModelResolver modelResolver() {
        // Jackson2のObjectMapperを使用して、スネークケースの設定を反映させる
        var objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return new ModelResolver(objectMapper);
    }

    /// Springdoc-openapiの定義
    @Bean
    OpenAPI springDocOpenAPI() {
        return new OpenAPI().info(new Info().title("非同期実行APIドキュメント")
            .description("非同期実行管理のためのAPIです。").version("v1.0"));
    }

}
