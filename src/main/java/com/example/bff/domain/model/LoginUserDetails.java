package com.example.bff.domain.model;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JDBCによる認証処理用UserDetails実装クラス
 *
 */
public class LoginUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = -3792232397594810206L;

    @Getter
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public LoginUserDetails(User user) {
        this.user = user;
        authorities = user != null ? List.of(new SimpleGrantedAuthority(user.getRole()))
            : Collections.emptyList();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public @NonNull String getUsername() {
        return user != null ? user.getUserId() : "";
    }

    @Override
    public boolean isAccountNonExpired() {
        // 期限切れ実装なし
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // アカウントロック実装なし
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // パスワード期限切れ実装なし
        return true;
    }

    @Override
    public boolean isEnabled() {
        // アカウント有効・無効実装なし
        return true;
    }

}
