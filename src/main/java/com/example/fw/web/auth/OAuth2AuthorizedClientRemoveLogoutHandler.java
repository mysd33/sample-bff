package com.example.fw.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class OAuth2AuthorizedClientRemoveLogoutHandler implements LogoutHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken) {
            authorizedClientService.removeAuthorizedClient(
                oauth2AuthenticationToken.getAuthorizedClientRegistrationId(),
                oauth2AuthenticationToken.getName()
            );
        }
        // 現状、バックチャネルログアウトの際には、OidcBackChannelLogoutAuthenticationがデフォルト修飾子のため、参照できないことから
        // instanceofによる判定式およびremoveAuthorizedClientの呼び出しは、バックチャネルログアウト時には実行されない。
    }

}
