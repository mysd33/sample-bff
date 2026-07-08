package com.example.bff.domain.service.login;

import com.example.bff.domain.model.OAuth2LoginUserDetails;
import com.example.bff.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/// GitHub等のOAuth2（非OIDC）のユーザ情報をアプリ内のLoginUserDetailsへマッピングするOAuth2UserService
@Service
@RequiredArgsConstructor
public class OAuth2LoginUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    /// アプリ内ユーザIDに対応するOAuth2の属性名
    /// GitHub の場合は "login"（GitHub ユーザ名）
    @Value("${example.security.oauth2.user-id-attribute:login}")
    String userIdAttribute;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        User user = new User();

        // ユーザIDの設定
        Object attrValue = oauth2User.getAttributes().get(userIdAttribute);
        String userId = attrValue instanceof String s ? s : null;
        if (userId == null || userId.isBlank()) {
            // フォールバック: プロバイダが指定したname属性
            userId = oauth2User.getName();
        }
        user.setUserId(userId);

        // ユーザ名の設定
        user.setUserName(userId);

        // 権限は一般ユーザ権限で固定化しておく
        user.setAdmin(false);
        user.setRole("ROLE_GENERAL");

        return new OAuth2LoginUserDetails(user, oauth2User);
    }
}
