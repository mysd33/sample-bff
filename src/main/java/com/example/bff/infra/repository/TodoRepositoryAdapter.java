package com.example.bff.infra.repository;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryAdapter implements TodoRepository {


    // Basic認証用のWebClientを使ったTodoRepositoryの実装クラス
    private final TodoRepositoryImplByWebClientBasicAuth todoRepositoryImplByWebClientBasicAuth;
    // OAuth2.0クライアント用のWebClientを使ったTodoRepositoryの実装クラス
    private final TodoRepositoryImplByWebClientOAuth2 todoRepositoryImplByWebClientOAuth2Auth;

    @Nullable
    private final OAuth2AuthorizedClientService authorizedClientService;


    @Override
    public Optional<Todo> findById(String todoId) {
        return getTodoRepository().findById(todoId);
    }

    @Override
    public Collection<Todo> findAllByUserId(String userId) {
        return getTodoRepository().findAllByUserId(userId);
    }

    @Override
    public void create(Todo todo) {
        getTodoRepository().create(todo);
    }

    @Override
    public boolean update(Todo todo) {
        return getTodoRepository().update(todo);
    }

    @Override
    public boolean delete(Todo todo) {
        return getTodoRepository().delete(todo);
    }


    private TodoRepository getTodoRepository() {
        if (resolveAccessToken()) {
            return todoRepositoryImplByWebClientOAuth2Auth;
        } else {
            return todoRepositoryImplByWebClientBasicAuth;
        }
    }

    /// Spring Securityで管理されているアクセストークンを取得する。存在しない場合はnullを返す
    private boolean resolveAccessToken() {
        if (authorizedClientService == null) {
            return false;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return false;
        }
        // サンプルAPでは、registration IDが、KeyCloakの場合のみアクセストークンを送信するようにする
        // (それ以外のIdPでは対応が難しいのでBasic認証にする)
        if (!TodoRepositoryImplByWebClientOAuth2.CLIENT_REGISTRATION_ID.equals(
            oauth2Authentication.getAuthorizedClientRegistrationId())) {
            return false;
        }
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauth2Authentication.getAuthorizedClientRegistrationId(),
            oauth2Authentication.getName());
        if (authorizedClient == null) {
            return false;
        }
        // アクセストークンが存在するか
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return accessToken != null && StringUtils.hasText(accessToken.getTokenValue());
    }
}
