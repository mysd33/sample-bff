package com.example.bff.app.web.login;

import com.example.bff.app.web.common.authentication.AuthenticationUtil;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/// ログイン機能のコントローラクラス
@Controller
@Slf4j
public class LoginController {

    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    @ModelAttribute
    public LoginForm setUpForm() {
        return new LoginForm();
    }

    /// ログイン画面のGETメソッド用処理
    @GetMapping("/login")
    public String getLogin() {
        // ログイン済みの場合
        if (AuthenticationUtil.isAuthenticated()) {
            // メニュー画面へ遷移
            return "redirect:/menu";
        }
        // ログイン画面へ遷移
        return "login/login";
    }

    /// ログイン処理
    @PostMapping("/login")
    public String postLogin(@Validated LoginForm form, BindingResult result) {
        // 入力チェックエラー時
        if (result.hasErrors()) {
            return "login/login";
        }
        // 入力チェックが問題なければSpringSecurityのログイン処理へ転送
        return "forward:/authenticate";
    }

    /// ログイン成功後のメニュー画面遷移処理
    @GetMapping("/menu")
    public String menu() {
        return "menu/menu";
    }

    /// 管理者用ユーザ管理ページ遷移用処理
    @GetMapping("/admin")
    public String admin() {
        return "redirect:/userList";
    }


    /// 外部IDプロバイダのログイン画面のページ遷移処理
    @GetMapping("/oidc-login")
    public String getOIDCLogin() {
        return "login/oidc-login";
    }

    /// OIDCでのログイン成功後の外部IDプロバイダのメニュー画面のページ遷移処理
    @GetMapping("/oidc-menu")
    public String getOIDCMenu(
        @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
        @AuthenticationPrincipal OAuth2User oidcUser) {
        appLogger.debug("username: {}", oidcUser.getName());
        appLogger.debug("userAttributes: {}", oidcUser.getAttributes());
        appLogger.debug("clientName: {}", authorizedClient.getClientRegistration().getClientName());
        return "menu/dummy-menu";
    }

}
