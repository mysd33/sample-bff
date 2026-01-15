package com.example.bff.domain.service.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.bff.domain.model.User;

/**
 * 
 * ユーザ管理機能のサービスインタフェース
 *
 */
public interface UserService {

    /**
     * ユーザ登録
     */
    boolean insert(User user);

    /**
     * ユーザ数取得
     */
    int count();

    /**
     * 全件取得
     */
    List<User> findAll();

    /**
     * 全件取得（ページネーション）
     */
    Page<User> findAllForPagination(Pageable pageable);

    /**
     * １件取得
     */
    User findOne(String userId);

    /**
     * １件更新
     */
    void updateOne(User user);

    /**
     * １件削除
     */
    void deleteOne(String userId);

}