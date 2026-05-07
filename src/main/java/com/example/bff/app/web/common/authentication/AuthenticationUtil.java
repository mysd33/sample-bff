package com.example.bff.app.web.common.authentication;

import com.example.bff.domain.model.LoginUserDetails;
import com.example.bff.domain.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/// 認証情報取得ユーティリティクラス
public class AuthenticationUtil {

    /// コンストラクタ
    private AuthenticationUtil() {
    }

    /// ログイン済みかどうか取得する
    ///
    /// @return trueならログイン済
    public static boolean isAuthenticated() {
        User loginUser = getLoginUser();
        return loginUser != null;
    }

    /// ログイン済みのユーザを取得する
    ///
    /// @return ログインユーザ
    public static @Nullable User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
            && authentication.getPrincipal() instanceof LoginUserDetails loginUserDetails) {
            return loginUserDetails.getUser();
        }
        return null;
    }

}
