package com.example.bff.domain.model;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;

/// GitHub等のOAuth2（非OIDC）認証でもLoginUserDetailsとして扱うためのUserDetails実装
public class OAuth2LoginUserDetails extends LoginUserDetails implements OAuth2User {

    private final transient OAuth2User delegate;

    public OAuth2LoginUserDetails(User user, OAuth2User delegate) {
        super(user);
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public @NonNull String getName() {
        return delegate.getName();
    }
}
