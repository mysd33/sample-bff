package com.example.bff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 
 * SpringSecurityの設定クラス
 *
 */
@Configuration
public class SecurityConfig {
	// Spring Security5.7より大幅に設定方法が変更された
	// https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
	// https://www.docswell.com/s/MasatoshiTada/KGVY9K-spring-security-intro

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// ログイン不要ページの設定
		http.authorizeRequests().antMatchers("/webjars/**").permitAll() // webjarsへアクセス許可
				.antMatchers("/css/**").permitAll()// cssへアクセス許可
				.antMatchers("/js/**").permitAll()// jsへアクセス許可
				.antMatchers("/h2-console/**").permitAll()
				.antMatchers("/actuator/**").permitAll()
				.antMatchers("/login").permitAll() // ログインページは直リンクOK
				.antMatchers("/admin").hasAuthority("ROLE_ADMIN") // アドミンユーザーに許可
				.anyRequest().authenticated(); // それ以外は直リンク禁止

		// フォーム認証によるログイン処理
		http.formLogin(login -> login
				.loginProcessingUrl("/login") // ログイン処理のパス
				.loginPage("/login") // ログインページの指定
				.failureUrl("/login") // ログイン失敗時の遷移先
				.usernameParameter("userId") // ログインページのユーザーID
				.passwordParameter("password") // ログインページのパスワード
				.defaultSuccessUrl("/menu", true) // ログイン成功後の遷移先
				.permitAll()
		)
				// ログアウト処理
				.logout(logout -> logout
						.logoutUrl("/logout") // ログアウトのURL						
						.logoutSuccessUrl("/") // ログアウト成功後のURL
				);
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
