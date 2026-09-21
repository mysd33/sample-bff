package com.example.bff.domain.model;

import java.io.Serial;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

/// GitHub等のOAuth2（非OIDC）認証でもLoginUserDetailsとして扱うためのUserDetails実装
public class OAuth2LoginUserDetails extends LoginUserDetails implements OAuth2User {

    @Serial
    private static final long serialVersionUID = 7439381295220914873L;
    
    private final DefaultOAuth2User delegate;

    public OAuth2LoginUserDetails(User user, DefaultOAuth2User delegate) {
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
