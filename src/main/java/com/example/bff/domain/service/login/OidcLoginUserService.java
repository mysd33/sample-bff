package com.example.bff.domain.service.login;

import com.example.bff.domain.model.OidcLoginUserDetails;
import com.example.bff.domain.model.User;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/// Keycloak/Google等のOIDC準拠プロバイダのユーザ情報をアプリ内のLoginUserDetailsへマッピングするOAuth2UserService
@Service
@RequiredArgsConstructor
public class OidcLoginUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    @Value("${example.security.realm-claim:realm_access}")
    private String realmClaimName;

    @Value("${example.security.role-claim:roles}")
    private String roleClaimName;

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

        // IDトークンに設定されたrealm_access.rolesをGrantedAuthorityにマッピング
        Map<String, Object> realmAccess = oidcUser.getClaimAsMap(realmClaimName);
        if (realmAccess != null) {
            var roles = (List<?>) realmAccess.get(roleClaimName);

            roles.forEach(role -> {
                var roleName = (String) role;
                if ("ADMIN".equals(roleName)) {
                    user.setAdmin(true);
                    user.setRole("ROLE_ADMIN");
                }
            });
        }
        if (!user.isAdmin()) {
            // それ以外は一般ユーザ
            user.setRole("ROLE_GENERAL");
        }
        // LoginUserDetailsにマッピング
        return new OidcLoginUserDetails(user, oidcUser);
    }
}
