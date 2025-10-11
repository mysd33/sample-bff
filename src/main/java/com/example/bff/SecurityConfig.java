package com.example.bff;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.fw.web.auth.config.AuthConfigPackage;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;

/**
 * 
 * SpringSecurityの設定クラス
 *
 */
@Configuration
@ComponentScan(basePackageClasses = { AuthConfigPackage.class })
@EnableWebSecurity
public class SecurityConfig {
    // Spring Security5.7より大幅に設定方法が変更された
    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    // https://www.docswell.com/s/MasatoshiTada/KGVY9K-spring-security-intro

    // Spring Securityのデバッグモード
    @Value("${example.websecurity.debug:false}")
    boolean webSecurityDebug;

    /**
     * Spring Securityのデバッグモードの設定
     * 
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(webSecurityDebug);
    }

    /**
     * Spring Securityによる認証認可設定
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // フォーム認証にによるログイン処理
        http.formLogin(login -> login.loginProcessingUrl("/authenticate") // ログイン処理のパス
                .loginPage("/login") // ログインページの指定
                .failureUrl("/login?error") // ログイン失敗時の遷移先
                .usernameParameter("userId") // ログインページのユーザーID
                .passwordParameter("password") // ログインページのパスワード
                .defaultSuccessUrl("/menu", true) // ログイン成功後の遷移先
                .permitAll())
                // ログアウト処理
                .logout(logout -> logout.logoutUrl("/logout") // ログアウトのURL
                        .logoutSuccessUrl("/")) // ログアウト成功後のURL
                // 認可設定
                .authorizeHttpRequests(
                        authz -> authz.requestMatchers(toStaticResources().atCommonLocations()).permitAll() // 静的リソースへアクセス許可
                                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll() // Spring Boot
                                                                                              // Actuatorのエンドポイントへアクセス許可
                                .requestMatchers("/login").permitAll() // ログインページへ認証なしでアクセス許可
                                .requestMatchers("/v3/api-docs/**").permitAll() // Springdoc-openapiのドキュメント認証なしでアクセス許可
                                .requestMatchers("/v3/api-docs*").permitAll() // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                                .requestMatchers("/swagger-ui/**").permitAll() // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                                .requestMatchers("/swagger-ui.html").permitAll() // Springdoc-openapiのドキュメントへ認証なしでアクセス許可
                                .requestMatchers("/api/**").permitAll()// REST APIへアクセス許可
                                .requestMatchers("/admin").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザのみ許可
                                .requestMatchers("/user*").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザのみ許可
                                .anyRequest().authenticated() // それ以外は認証が必要
                )
                // REST APIはCSRF保護不要
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    /**
     * H2 Consoleのアクセス許可対応
     */
    @Profile("dev")
    @Order(1)
    @Bean
    SecurityFilterChain securityFilterChainForH2Console(HttpSecurity http) throws Exception {
        // H2 ConsoleのURLに対して
        http.securityMatcher(toH2Console())//
                .authorizeHttpRequests(
                        // 認証不要でアクセス許可
                        authz -> authz.anyRequest().permitAll())
                // CSRF保護不要
                .csrf(csrf -> csrf.disable())
                // H2 Consoleの表示ではframeタグを使用しているのでX-FrameOptionsを無効化
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable());
        return http.build();
    }

    /**
     * パスワードエンコーダ
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // この例では、以前のサンプルAPのパスワードデータの互換性のため、接頭辞に{id}が付与されてない場合はBCryptを使用するよう設定
        ((DelegatingPasswordEncoder) passwordEncoder).setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return passwordEncoder;
    }

    /**
     * Spring Securityの可観測性無効化（ADOTにおける不具合回避対応）
     * 
     */
    @Profile("adot")
    @Bean
    ObservationRegistryCustomizer<ObservationRegistry> noSpringSecurityObservations() {
        ObservationPredicate predicate = (name, context) -> !name.startsWith("spring.security.");
        return (registry) -> registry.observationConfig().observationPredicate(predicate);
    }

}
