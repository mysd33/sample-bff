package com.example.bff.infra.repository;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoRepositoryAdapter implements TodoRepository {

    // OAuth2のクライアントを扱う場合のヘルパークラス
    private final OAuth2Helper oAuth2Helper;

    // Basic認証用のWebClientを使ったTodoRepositoryの実装クラス
    private final TodoRepositoryImplByWebClientBasicAuth todoRepositoryImplByWebClientBasicAuth;
    // OAuth2.0クライアント用のWebClientを使ったTodoRepositoryの実装クラス
    private final TodoRepositoryImplByWebClientOAuth2 todoRepositoryImplByWebClientOAuth2Auth;

    @Override
    public Optional<Todo> findById(String todoId) {
        return todoRepository().findById(todoId);
    }

    @Override
    public Collection<Todo> findAllByUserId(String userId) {
        return todoRepository().findAllByUserId(userId);
    }

    @Override
    public void create(Todo todo) {
        todoRepository().create(todo);
    }

    @Override
    public boolean update(Todo todo) {
        return todoRepository().update(todo);
    }

    @Override
    public boolean delete(Todo todo) {
        return todoRepository().delete(todo);
    }


    private TodoRepository todoRepository() {
        return oAuth2Helper.isWebAPIByOAuth() ?
            todoRepositoryImplByWebClientOAuth2Auth : todoRepositoryImplByWebClientBasicAuth;
    }


}
