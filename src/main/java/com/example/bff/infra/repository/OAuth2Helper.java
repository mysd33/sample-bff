package com.example.bff.infra.repository;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/// OAuth2のクライアントを扱う場合のヘルパークラス
@Component
@RequiredArgsConstructor
public class OAuth2Helper {

    public static final String CLIENT_REGISTRATION_ID = "keycloak";

    @Nullable
    private final OAuth2AuthorizedClientService authorizedClientService;

    /// Spring Securityで管理されているアクセストークンを取得する。存在しない場合はnullを返す
    public boolean resolveAccessToken() {
        if (authorizedClientService == null) {
            return false;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return false;
        }
        // サンプルAPでは、registration IDが、KeyCloakの場合のみアクセストークンを送信するようにする
        // (それ以外のIdPでは対応が難しいのでBasic認証にする)
        if (!CLIENT_REGISTRATION_ID.equals(
            oauth2Authentication.getAuthorizedClientRegistrationId())) {
            return false;
        }
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauth2Authentication.getAuthorizedClientRegistrationId(),
            oauth2Authentication.getName());
        if (authorizedClient == null) {
            return false;
        }
        // アクセストークンが存在するか
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return StringUtils.hasText(accessToken.getTokenValue());
    }
}
