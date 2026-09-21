package com.example.bff.domain.model;

import java.io.Serial;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/// OIDC認証でもフォーム認証と同じLoginUserDetailsとして扱うためのUserDetails実装
public class OidcLoginUserDetails extends LoginUserDetails implements OidcUser {

    @Serial
    private static final long serialVersionUID = 2152056894130861078L;

    private final DefaultOidcUser delegate;

    public OidcLoginUserDetails(User user, DefaultOidcUser delegate) {
        super(user);
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public @NonNull String getName() {
        return delegate.getName();
    }
}
