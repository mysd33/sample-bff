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
    }

}
