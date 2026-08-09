package com.example.bff.infra.repository;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

/// OAuth2のクライアントを扱う場合のヘルパークラス
@Component
@RequiredArgsConstructor
public class OAuth2Helper {

    public static final String CLIENT_REGISTRATION_ID = "keycloak";

    @Nullable
    private final OAuth2AuthorizedClientService authorizedClientService;

    /// OAuth2.0でのアクセストークンによるWebAPIアクセスかどうかを返却する
    public boolean isWebAPIByOAuth() {
        if (authorizedClientService == null) {
            return false;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return false;
        }
        // サンプルAPでは、registration IDが、KeyCloakの場合のみアクセストークンを送信するようにする
        // (それ以外のIdPでは対応が難しいのでBasic認証にする)
        return CLIENT_REGISTRATION_ID.equals(
            oauth2Authentication.getAuthorizedClientRegistrationId());
    }
}
