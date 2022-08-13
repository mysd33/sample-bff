package com.example.bff.domain.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bff.domain.model.User;
import com.example.bff.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;


/**
 * 
 * ユーザ管理機能のサービス実装クラス
 *
 */
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public boolean insert(User user) {		
		String password = passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		return repository.insert(user);
	}

	/**
	 * カウント用メソッド.
	 */
	@Transactional(readOnly = true)
	@Override
	public int count() {
		return repository.count();
	}

	/**
	 * 全件取得用メソッド.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<User> findAll() {
		return repository.findAll();
	}

	/**
	 * 全件取得（ページネーション）用メソッド.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<User> findAllForPagination(Pageable pageable) {
		int total = repository.count();
		List<User> users;
		if (total > 0) {
			users = repository.findAllForPagination(pageable);
		} else {
			users = Collections.emptyList();
		}
		return new PageImpl<>(users, pageable, total);
	}

	/**
	 * １件取得用メソッド.
	 */
	@Override
	@Transactional(readOnly = true)
	public User findOne(String userId) {
		return repository.findOne(userId);
	}

	/**
	 * １件更新用メソッド.
	 */
	@Override
	public boolean updateOne(User user) {		
		String password = passwordEncoder.encode(user.getPassword());		
		user.setPassword(password);
		return repository.updateOne(user);
	}

	/**
	 * １件削除用メソッド.
	 */
	@Override
	public boolean deleteOne(String userId) {
		return repository.deleteOne(userId);
	}

}
