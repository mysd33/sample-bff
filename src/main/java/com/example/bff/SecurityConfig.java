package com.example.bff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 
 * SpringSecurityの設定クラス
 *
 */
@EnableWebSecurity
public class SecurityConfig {
    // Spring Security5.7より大幅に設定方法が変更された
    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    // https://www.docswell.com/s/MasatoshiTada/KGVY9K-spring-security-intro

    /**
     * Spring Securityによる認証認可設定
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
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
						.logoutSuccessUrl("/")) // ログアウト成功後のURL
				// 認可設定
				.authorizeHttpRequests(authz -> authz.antMatchers("/webjars/**").permitAll() // webjarsへアクセス許可
						.antMatchers("/css/**").permitAll()// cssへアクセス許可
						.antMatchers("/js/**").permitAll()// jsへアクセス許可
						.antMatchers("/login").permitAll() // ログインページは直リンクOK
						.antMatchers("/actuator/**").permitAll() // actuatorのAPIへアクセス許可
						.antMatchers("/v3/api-docs/**").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
						.antMatchers("/v3/api-docs*").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
						.antMatchers("/swagger-ui/**").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
						.antMatchers("/swagger-ui.html").permitAll() // Springdoc-openapiのドキュメントへのアクセス許可
						.antMatchers("/api/**").permitAll()// REST APIへアクセス許可
						.antMatchers("/admin").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
						.antMatchers("/user*").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
						.anyRequest().authenticated() // それ以外は認証・認可が必要
				)
				// REST APIはCSRF保護不要
				.csrf().ignoringAntMatchers("/api/**");
		// @formatter:on
        return http.build();
    }

    /**
     * H2 Consoleのアクセス許可対応
     */
    @Profile("dev")
    @Order(1)
    @Bean
    public SecurityFilterChain securityFilterChainForH2Console(HttpSecurity http) throws Exception {
        // @formatter:off
		//H2 ConsoleのURLに対して
		http.antMatcher("/h2-console/**")
			.authorizeHttpRequests(
				// 認証不要でアクセス許可
				authz -> authz.anyRequest().permitAll())
			// CSRF保護不要			
			.csrf().disable()
			// H2 Consoleの表示ではframeタグを使用しているのでX-FrameOptionsを無効化
			.headers().frameOptions().disable();
        // @formatter:on		
        return http.build();
    }

    /**
     * パスワードエンコーダ
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
