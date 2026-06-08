package com.example.bff.domain.service.user;

import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.model.User;
import com.example.bff.domain.repository.UserRepository;
import com.example.fw.common.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/// ユーザ管理機能のサービス実装クラス
@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /// ユーザ登録
    @Override
    public boolean insert(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        try {
            return repository.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(e, MessageIds.W_EX_8005, user.getUserId());
        }
    }

    /// カウント用メソッド.
    @Transactional(readOnly = true)
    @Override
    public int count() {
        return repository.count();
    }

    /// 全件取得用メソッド.
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    /// 全件取得（ページネーション）用メソッド.
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllForPagination(Pageable pageable) {
        var total = repository.count();
        List<User> users;
        if (total > 0) {
            users = repository.findAllForPagination(pageable);
        } else {
            users = List.of();
        }
        return new PageImpl<>(users, pageable, total);
    }

    /// １件取得用メソッド.
    @Override
    @Transactional(readOnly = true)
    public User findOne(String userId) {
        return repository.findOne(userId);
    }

    /// １件更新用メソッド.
    @Override
    public void updateOne(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        var result = repository.updateOne(user);
        if (!result) {
            // 楽観的オフラインロックにより更新件数0件の場合は、同時更新エラーとする。
            throw new BusinessException(MessageIds.W_EX_8008, user.getUserId());
        }
    }

    /// １件削除用メソッド.
    @Override
    public void deleteOne(User user) {
        // TODO:自分のユーザ情報は削除できないようにする

        var result = repository.deleteOne(user);
        if (!result) {
            // 楽観的オフラインロックにより更新件数0件の場合は、同時更新エラーとする。
            throw new BusinessException(MessageIds.W_EX_8009, user.getUserId());
        }
    }

}
