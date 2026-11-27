package com.example.fw.web.auth.config;

import com.example.fw.common.constants.FrameworkConstants;
import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientXrayFilter;
import com.example.fw.web.auth.ExternalStoreOidcSessionRegistry;
import com.example.fw.web.auth.OAuth2AuthorizedClientRemoveLogoutHandler;
import com.example.fw.web.auth.OidcSessionDataRepository;
import com.example.fw.web.auth.RedisOidcSessionDataRepository;
import com.example.fw.web.auth.TransactionalJdbcOAuth2AuthorizedClientService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcBackChannelLogoutHandler;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionRegistry;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

/// OIDC OAuthクライアント関連の設定クラス
@Configuration
@ConditionalOnProperty(name = OidcClientConfig.OIDC_ENABLED_PROPERTY, havingValue = "true")
public class OidcClientConfig {

    private static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "oauth2";
    static final String OIDC_ENABLED_PROPERTY = PROPERTY_PREFIX + ".enabled";
    static final String OAUTH2_AUTHORIZED_CLIENT_SERVICE_STORE_TYPE =
        PROPERTY_PREFIX + ".authorized-client-service.store-type";

    /// RelyingPartyの複数ノード間で共有するための外部のストアに管理されたOidcSessionRegistry実装の定義
    @ConditionalOnProperty(name = OidcClientConfig.OAUTH2_AUTHORIZED_CLIENT_SERVICE_STORE_TYPE, havingValue = "jdbc", matchIfMissing = true)
    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
        JdbcOperations jdbcOperations,
        ClientRegistrationRepository clientRegistrationRepository) {
        return new TransactionalJdbcOAuth2AuthorizedClientService(
            new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository));
    }

    @Bean
    OAuth2AuthorizedClientRemoveLogoutHandler authorizedClientRemoveLogoutHandler(
        OAuth2AuthorizedClientService authorizedClientService) {
        return new OAuth2AuthorizedClientRemoveLogoutHandler(authorizedClientService);
    }

    /// RESTクライアント関連の設定クラス（OIDC OAuth 用）
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

    // Spring Session Data Redisが利用される場合の設定クラス
    @Configuration
    @ConditionalOnClass(name = "org.springframework.session.data.redis.RedisIndexedSessionRepository")
    static class SpringSessionConfig {

        /// RelyingPartyの複数ノード間で共有するための外部のストアに管理されたOidcSessionRegistry実装の定義
        @Bean
        OidcSessionRegistry oidcSessionRegistry(
            OidcSessionDataRepository oidcSessionDataRepository) {
            // 標準機能がないため、独自実装のExternalStoreOidcSessionRegistryを利用する。
            // Spring Session Redisが利用される場合として、Redis実装のOidcSessionRegistryが利用される。
            return new ExternalStoreOidcSessionRegistry(oidcSessionDataRepository);
        }

        /// OidcSession情報（クライアントセッションIDとOpenID ProviderとのセッションIDを紐づける情報）を
        /// Redisを利用し永続化するためのリポジトリの定義
        @Bean
        OidcSessionDataRepository oidcSessionDataRepository(
            StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            @Value("${server.servlet.session.timeout:30m}") Duration sessionTimeout) {
            return new RedisOidcSessionDataRepository(redisTemplate, objectMapper, sessionTimeout);
        }

        /// デフォルトではセッションログアウトエンドポイントのセッションIDはJSESSIONIDでCookieに保存されるが
        /// 商用環境では、Spring Session Redisが利用されるるためセッションIDをSESSIONでCookieに保存するように設定する。
        /// これを設定しないとバックチャネルログアウト時にセッションIDが取得できず、ログアウト処理が失敗する。
        @Bean
        OidcBackChannelLogoutHandler oidcLogoutHandler(OidcSessionRegistry oidcSessionRegistry) {
            // https://docs.spring.io/spring-security/reference/servlet/oauth2/login/logout.html#_customizing_the_session_logout_cookie_name
            OidcBackChannelLogoutHandler logoutHandler = new OidcBackChannelLogoutHandler(
                oidcSessionRegistry);
            logoutHandler.setSessionCookieName("SESSION");
            return logoutHandler;
        }

        @Bean
        public CookieSerializer cookieSerializer() {
            DefaultCookieSerializer serializer = new DefaultCookieSerializer();
            // Spring SessionのDefaultCookieSerializerは、
            // デフォルトで Cookie の値を Base64エンコードして送受信する。
            // しかし、OidcBackChannelLogoutHandler は内部リクエストを作成する際、
            // セッションIDを生の文字列のままCookieヘッダーに設定するため、
            // Spring Session 側で「Base64 デコードできない、または壊れたセッションID」と判定され、
            // Redis 上のセッションが見つからずに削除がスキップされてしまう。
            // OidcBackChannelLogoutHandler と整合性を保つため Base64 エンコードを無効化する。
            // https://github.com/spring-projects/spring-security/issues/14904
            serializer.setUseBase64Encoding(false);
            return serializer;
        }


    }
}
