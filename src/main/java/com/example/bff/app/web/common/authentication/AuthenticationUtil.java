package com.example.bff.app.web.common.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.bff.domain.model.LoginUserDetails;
import com.example.bff.domain.model.User;

/**
 * 認証情報取得ユーティリティクラス
 *
 */
public class AuthenticationUtil {
    /**
     * コンストラクタ
     */
    private AuthenticationUtil() {
    }

    /**
     * ログイン済みかどうか取得する
     * @return trueならログイン済
     */
    public static boolean isAuthenticated() {
        User loginUser = getLoginUser();
        return loginUser != null;
    }

    /**
     * ログイン済みのユーザを取得する
     * @return ログインユーザ
     */
    public static User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof LoginUserDetails) {
            LoginUserDetails loginUserDetails = LoginUserDetails.class.cast(authentication.getPrincipal());
            return loginUserDetails.getUser();
        }
        return null;
    }

}
