package com.example.bff.domain.service.login;

import com.example.bff.domain.model.OidcLoginUserDetails;
import com.example.bff.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/// Keycloak/Google等のOIDC準拠プロバイダのユーザ情報をアプリ内のLoginUserDetailsへマッピングするサービス
@Service
@RequiredArgsConstructor
public class OidcLoginUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    /// OIDC準拠プロバイダのクレームからアプリ内ユーザIDを取得する属性名
    /// Keycloak: "preferred_username"（デフォルト）
    /// Google: "email"（preferred_username を持たないため）
    @Value("${example.security.oidc.user-id-claim:preferred_username}")
    String userIdClaim;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        User user = new User();

        // ユーザIDの設定
        String userId = oidcUser.getClaimAsString(userIdClaim);
        if (userId == null || userId.isBlank()) {
            userId = oidcUser.getSubject();
        }
        if (userId == null || userId.isBlank()) {
            userId = oidcUser.getEmail();
        }
        user.setUserId(userId);
        user.setUserName(oidcUser.getFamilyName() + " " + oidcUser.getGivenName());

        // 権限は一般ユーザ権限で固定化しておく
        user.setAdmin(false);
        user.setRole("ROLE_GENERAL");

        // LoginUserDetailsにマッピング
        return new OidcLoginUserDetails(user, oidcUser);
    }
}
