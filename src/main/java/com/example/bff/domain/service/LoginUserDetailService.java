package com.example.bff.domain.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bff.domain.model.LoginUserDetails;
import com.example.bff.domain.model.User;
import com.example.bff.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * JDBCによる認証処理用UserDetailService実装クラス 
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailService implements UserDetailsService {
	private final UserRepository repository;
	
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = repository.findOne(userId);
		return new LoginUserDetails(user);
	}

}
