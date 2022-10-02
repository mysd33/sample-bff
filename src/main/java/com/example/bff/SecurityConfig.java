package com.example.bff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
	
	@Profile("dev")
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/h2-console/**");		// h2-consoleにアクセス許可
    }

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// ログイン不要ページの設定
		http.authorizeRequests().antMatchers("/webjars/**").permitAll() // webjarsへアクセス許可
				.antMatchers("/css/**").permitAll()// cssへアクセス許可
				.antMatchers("/js/**").permitAll()// jsへアクセス許可
				.antMatchers("/api/**").permitAll()// REST APIへアクセス許可
				.antMatchers("/login").permitAll() // ログインページは直リンクOK
				.antMatchers("/actuator/**").permitAll() //actuatorのAPIへアクセス許可
				.antMatchers("/admin").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
				.antMatchers("/user*").hasAuthority("ROLE_ADMIN") // ユーザ管理画面は管理者ユーザーのみ許可
				.anyRequest().authenticated(); // それ以外は認証・認可が必要

		// フォーム認証によるログイン処理
		http.formLogin(login -> login
				.loginProcessingUrl("/authenticate") // ログイン処理のパス
				.loginPage("/login") // ログインページの指定
				.failureUrl("/login?error") // ログイン失敗時の遷移先
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
