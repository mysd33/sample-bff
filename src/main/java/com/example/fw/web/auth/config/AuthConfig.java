package com.example.fw.web.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.fw.web.auth.SpringSecurityUserNameProvider;
import com.example.fw.web.auth.UserNameProvider;

/**
 * 認証関連の設定クラス
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
public class AuthConfig {

    @Bean
    UserNameProvider userNameProvider() {
        return new SpringSecurityUserNameProvider();
    }

}
