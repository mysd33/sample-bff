package com.example.fw.common.httpclient.config;

import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/// RESTクライアント関連の設定クラス（OIDC OAuth 用）
@Profile("oidc")
@Configuration
public class OidcOAuthWebClientConfig {

    /// WebClientクラス
    @Profile("!xray")
    @Bean
    WebClient webClientWithOIDC(WebClient.Builder builder, WebClientLoggingFilter loggingFilter,
        OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction filter =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return builder.apply(filter.oauth2Configuration())
            .filter(loggingFilter.filter()).build();
    }

    /// WebClientクラス（X-Rayトレーシング SDK）<br>
    ///
    /// @deprecated X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClient webClientWithOIDCAndXRay(WebClient.Builder builder,
        WebClientLoggingFilter loggingFilter,
        WebClientXrayFilter xrayFilter, OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction filter =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return builder.apply(filter.oauth2Configuration())
            .filter(loggingFilter.filter()).filter(xrayFilter.filter()).build();
    }

    /// WebClientでのAWS X-Ray SDKのHttpクライアントトレーシング設定<br>
    ///
    /// @deprecated X-Ray SDKは 2027 年 2 月 25 日にサポート終了となるため削除予定
    @Deprecated(forRemoval = true)
    @Profile("xray")
    @Bean
    WebClientXrayFilter webClientXrayFilter() {
        return new WebClientXrayFilter();
    }
}
