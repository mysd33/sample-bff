package com.example.bff.domain.model;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 *  JDBCによる認証処理用UserDetails実装クラス 
 *
 */
public class LoginUserDetails implements UserDetails {
	private static final long serialVersionUID = -3792232397594810206L;
	
	@Getter
	private final User user;
	private final Collection<? extends GrantedAuthority> authorities;
	
	public LoginUserDetails(User user) {
		this.user = user;		
		authorities = Arrays.asList(new SimpleGrantedAuthority(user.getRole()));
	}
		
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {		
		return user.getUserId();
	}

	@Override
	public boolean isAccountNonExpired() {
		//期限切れ実装なし
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		//アカウントロック実装なし
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		//パスワード期限切れ実装なし
		return true;
	}

	@Override
	public boolean isEnabled() {
		//アカウント有効・無効実装なし
		return true;
	}

}
