package com.example.bff;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toH2Console;
import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

import com.example.fw.web.auth.config.AuthConfigPackage;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationRegistryCustomizer;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/// SpringSecurityの設定クラス
@Configuration
@ComponentScan(basePackageClasses = {AuthConfigPackage.class})
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring Securityのデバッグモード
    @Value("${example.security.debug:false}")
    private boolean webSecurityDebug;

    /// Spring Securityのデバッグモードの設定
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(webSecurityDebug);
    }

    /// Spring SecurityによるOIDC併用の認証認可設定
    @Bean
    @ConditionalOnProperty(name = "example.oidc.enabled", havingValue = "true")
    SecurityFilterChain securityFilterChainForOIDC(HttpSecurity http,
        ClientRegistrationRepository clientRegistrationRepository//,
        //OidcLoginUserService oidcLoginUserService,
        //OAuth2LoginUserService oauth2LoginUserService
    ) {
        http
            // フォーム認証によるログイン処理
            .formLogin(login -> login.loginProcessingUrl("/authenticate") // ログイン処理のパス
                .loginPage("/login") // ログインページの指定
                .failureUrl("/login?error") // ログイン失敗時の遷移先
                .usernameParameter("userId") // ログインページのユーザーID
                .passwordParameter("password") // ログインページのパスワード
                .defaultSuccessUrl("/menu", true) // ログイン成功後の遷移先
                .permitAll())
            // OIDC/OAuth2認証によるログイン処理
            .oauth2Login(login ->
                // ログインのページ
                login.loginPage("/oidc-login")
                    // Bean定義されていれば、明示的な指定不要
                    //.userInfoEndpoint(userInfo -> userInfo
                    // OIDC準拠プロバイダ（Google等）用
                    //.oidcUserService(oidcLoginUserService)
                    // OAuth2のみのプロバイダ（GitHub等）用
                    //.userService(oauth2LoginUserService)
                    //)
                    .defaultSuccessUrl("/oidc-menu", true)
            )
            // OIDCのバックチャネルログアウト処理の設定
            .oidcLogout(logout -> logout.backChannel(Customizer.withDefaults()))
            // ログアウト処理（フォーム認証、OIDCのRP起点のログアウト処理共通）
            .logout(logout -> logout.logoutUrl("/logout") // ログアウトのURL
                .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
            )
            // OAuthClientの設定
            .oauth2Client(Customizer.withDefaults());
        // 認可設定
        configureAuthorization(http, true);
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(
        ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // ログアウト成功後の遷移先URLの指定
        // OIDCプロバイダはpost_logout_redirect_uriに絶対URLが必要なため{baseUrl}を使用する。
        // フォーム認証ユーザー（OidcUserでない場合）もこのURLへフォールバックされる。
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");

        return oidcLogoutSuccessHandler;
    }

    // Spring Security5.7より大幅に設定方法が変更された
    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    // https://www.docswell.com/s/MasatoshiTada/KGVY9K-spring-security-intro

    /// Spring SecurityによるForm認証のみの認証認可設定
    @Bean
    @ConditionalOnProperty(name = "example.oidc.enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // フォーム認証によるログイン処理
        http.formLogin(login -> login.loginProcessingUrl("/authenticate") // ログイン処理のパス
                .loginPage("/login") // ログインページの指定
                .failureUrl("/login?error") // ログイン失敗時の遷移先
                .usernameParameter("userId") // ログインページのユーザーID
                .passwordParameter("password") // ログインページのパスワード
                .defaultSuccessUrl("/menu", true) // ログイン成功後の遷移先
                .permitAll())
            // ログアウト処理
            .logout(logout -> logout.logoutUrl("/logout") // ログアウトのURL
                .logoutSuccessUrl("/")); // ログアウト成功後のURL
        // 認可設定
        configureAuthorization(http, false);
        return http.build();
    }

    /// 認可設定の共通処理
    private void configureAuthorization(HttpSecurity http, boolean includeOidcLogin) {
        http.authorizeHttpRequests(
                authz -> {
                    authz
                        // 静的リソースへアクセス許可
                        .requestMatchers(toStaticResources().atCommonLocations()).permitAll()
                        // Spring Boot Actuatorのエンドポイントへアクセス許可
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        // ログインページへ認証なしでアクセス許可
                        .requestMatchers("/login").permitAll();
                    if (includeOidcLogin) {
                        // 外部IdP用のログイン画面へ認証なしでアクセス許可
                        authz.requestMatchers("/oidc-login").permitAll();
                    }
                    authz
                        // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs*").permitAll()
                        // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                        .requestMatchers("/swagger-ui/**").permitAll()
                        // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                        .requestMatchers("/swagger-ui.html").permitAll()
                        // ユーザ管理画面は管理者ユーザのみ許可
                        .requestMatchers("/admin").hasAuthority("ROLE_ADMIN")
                        // ユーザ管理画面は管理者ユーザのみ許可
                        .requestMatchers("/user*").hasAuthority("ROLE_ADMIN")
                        // REST APIへ認証なしでアクセス許可（サンプルAPでの暫定）
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated(); // それ以外は認証が必要
                })
            // REST APIはCSRF保護不要（サンプルAPでの暫定）
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
    }

    /// H2 Consoleのアクセス許可対応
    @Profile("dev")
    @Order(1)
    @Bean
    SecurityFilterChain securityFilterChainForH2Console(HttpSecurity http) {
        // H2 ConsoleのURLに対して
        http.securityMatcher(toH2Console())//
            .authorizeHttpRequests(
                // 認証不要でアクセス許可
                authz -> authz.anyRequest().permitAll())
            // CSRF保護不要
            .csrf(AbstractHttpConfigurer::disable)
            // H2 Consoleの表示ではframeタグを使用しているのでX-FrameOptionsを無効化
            .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable());
        return http.build();
    }

    /// パスワードエンコーダ
    @Bean
    PasswordEncoder passwordEncoder() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // この例では、以前のサンプルAPのパスワードデータの互換性のため、接頭辞に{id}が付与されてない場合はBCryptを使用するよう設定
        ((DelegatingPasswordEncoder) passwordEncoder).setDefaultPasswordEncoderForMatches(
            new BCryptPasswordEncoder());
        return passwordEncoder;
    }

    /// Spring Securityの可観測性無効化（ADOTにおける不具合回避対応）
    @Profile("adot")
    @Bean
    ObservationRegistryCustomizer<ObservationRegistry> noSpringSecurityObservations() {
        ObservationPredicate predicate = (name, _) -> !name.startsWith("spring.security.");
        return registry -> registry.observationConfig().observationPredicate(predicate);
    }

}
