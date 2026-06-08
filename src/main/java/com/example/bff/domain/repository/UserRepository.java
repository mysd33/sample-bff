package com.example.bff.domain.repository;

import com.example.bff.domain.model.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

/// ユーザリポジトリインタフェース MyBatisにより実現
@Mapper
public interface UserRepository {

    /// ユーザを登録する
    ///
    /// @param user ユーザ情報
    /// @return 登録結果
    boolean insert(User user);

    /// ユーザ件数を取得する
    ///
    /// @return 件数
    int count();

    /// ユーザIDに一致するユーザ情報を取得する
    ///
    /// @param userId ユーザID
    /// @return 取得結果
    User findOne(String userId);

    /// ユーザを全件取得する
    ///
    /// @return 取得結果
    List<User> findAll();

    /// ユーザを全件取得する
    ///
    /// @return 取得結果
    List<User> findAllForPagination(Pageable pageable);

    /// ユーザ情報を更新する
    ///
    /// @param user ユーザ情報
    /// @return 更新結果
    boolean updateOne(User user);

    /// ユーザIDに一致するユーザ情報を削除する
    ///
    /// @param user ユーザ情報
    /// @return 削除結果
    boolean deleteOne(User user);
}
