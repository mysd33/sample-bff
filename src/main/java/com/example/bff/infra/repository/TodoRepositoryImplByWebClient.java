package com.example.bff.infra.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.model.TodoList;
import com.example.bff.domain.repository.TodoRepository;
import com.example.bff.infra.common.httpclient.CircuitBreakerErrorFallback;
import com.example.bff.infra.common.httpclient.WebClientResponseErrorHandler;
import com.example.fw.common.exception.BusinessException;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

//TODO: アクセストークンの有効期限切れエラーのときには、リフレッシュトークンを使ってアクセストークンを更新する処理を追加する

/// TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
@XRayEnabled
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClient implements TodoRepository {

    private static final String KEYCLOAK_REGISTRATION_ID = "keycloak";

    private final WebClient webClient;
    private final WebClientResponseErrorHandler responseErrorHandler;

    @Nullable
    private final OAuth2AuthorizedClientService authorizedClientService;

    // サーキットブレーカ
    // （参考）https://spring.io/projects/spring-cloud-circuitbreaker
    @SuppressWarnings("rawtypes")
    private final ReactiveCircuitBreakerFactory cbFactory;
    // リトライ回数
    @Value("${example.api.retry.max-attempts:3}")
    int maxAttempts;
    // エクスポネンシャルバックオフによる初回待機時間
    @Value("${example.api.retry.min-backoff:200}")
    long minBackoff;

    @Value("${example.api.backend.url}")
    private String backendUrl;

    // Basic認証のユーザー名
    @Value("${example.api.backend.basic-auth.username:}")
    private String basicAuthUsername;

    // Basic認証のパスワード
    @Value("${example.api.backend.basic-auth.password:}")
    private String basicAuthPassword;

    // WebClient(WebFlux)版の実装の参考ページ
    // https://news.mynavi.jp/techplus/article/techp5348/
    // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
    // https://spring.pleiades.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
    // https://medium.com/a-developers-odyssey/spring-web-client-exception-handling-cd93cf05b76

    /// Spring Securityで管理されているアクセストークンを取得する。存在しない場合はnullを返す
    private OAuth2AccessToken resolveAccessToken() {
        if (authorizedClientService == null) {
            return null;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            return null;
        }
        // サンプルAPではKeyCloakの場合のみアクセストークンを送信するようにする
        // (それ以外のIdPでは対応が難しいのでBasic認証にする)
        if (!KEYCLOAK_REGISTRATION_ID.equals(
            oauth2Authentication.getAuthorizedClientRegistrationId())) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauth2Authentication.getAuthorizedClientRegistrationId(),
            oauth2Authentication.getName());
        if (authorizedClient == null) {
            return null;
        }
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken == null || !StringUtils.hasText(accessToken.getTokenValue())) {
            return null;
        }
        return accessToken;
    }

    /// アクセストークンが存在すればBearerヘッダーを、なければBasic認証ヘッダーを設定する
    private void setAuthHeader(HttpHeaders headers) {
        OAuth2AccessToken accessToken = resolveAccessToken();
        if (accessToken != null) {
            // OAuth2アクセストークンが存在する場合はBearerヘッダーを設定
            headers.setBearerAuth(accessToken.getTokenValue());
        } else if (StringUtils.hasText(basicAuthUsername) && StringUtils.hasText(
            basicAuthPassword)) {
            // アクセストークンがない場合はBasic認証ヘッダーを設定
            headers.setBasicAuth(basicAuthUsername, basicAuthPassword);
        }
    }

    /// アクセストークンが存在する場合はv2、なければv1のURLを返す
    private String todosUrl() {
        String version = resolveAccessToken() != null ? "/api/v2/todos" : "/api/v1/todos";
        return UriComponentsBuilder.fromUriString(backendUrl).path(version).build().toUriString();
    }

    /// アクセストークンが存在する場合はv2、なければv1のURLを返す
    private String todoByIdUrl() {
        String version =
            resolveAccessToken() != null ? "/api/v2/todos/{todoId}" : "/api/v1/todos/{todoId}";
        return UriComponentsBuilder.fromUriString(backendUrl).path(version).build().toUriString();
    }


    @Override
    public Optional<Todo> findById(String todoId) {

        var todoMono = webClient.get().uri(todoByIdUrl(), todoId)//
            .headers(this::setAuthHeader)//
            .retrieve()//
            .onStatus(HttpStatusCode::is4xxClientError,
                responseErrorHandler::createClientErrorException)//
            .onStatus(HttpStatusCode::is5xxServerError,
                responseErrorHandler::createServerErrorException) //
            .bodyToMono(Todo.class)//
            // エクスポネンシャルバックオフによるリトライ
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                .filter(th -> !(th instanceof BusinessException)))//
            // サーキットブレーカによる処理
            .transform(it -> cbFactory.create("todo_findById").run(it,
                CircuitBreakerErrorFallback.returnMonoBusinessException()));
        return todoMono.blockOptional();

    }

    @Override
    public Collection<Todo> findAllByUserId(String userId) {
        var uri =
            UriComponentsBuilder.fromUriString(todosUrl()).queryParam("user_id", userId).build();
        var todoListMono = webClient.get().uri(uri.toUri())//
            .headers(this::setAuthHeader)//
            .retrieve()//
            .onStatus(HttpStatusCode::is4xxClientError,
                responseErrorHandler::createClientErrorException)//
            .onStatus(HttpStatusCode::is5xxServerError,
                responseErrorHandler::createServerErrorException) //
            .bodyToMono(TodoList.class)//
            // エクスポネンシャルバックオフによるリトライ
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                .filter(th -> !(th instanceof BusinessException)))
            // サーキットブレーカによる処理
            // Fallback時にエラーとせずに空のリストを例
            .transform(it -> cbFactory.create("todo_findAll").run(it,
                _ -> Mono.just(new TodoList())));
        return todoListMono.block();
    }

    @Override
    public void create(Todo todo) {
        webClient.post().uri(todosUrl())//
            .headers(this::setAuthHeader)//
            .contentType(MediaType.APPLICATION_JSON).bodyValue(todo)//
            .retrieve()//
            .onStatus(HttpStatusCode::is4xxClientError,
                responseErrorHandler::createClientErrorException)//
            .onStatus(HttpStatusCode::is5xxServerError,
                responseErrorHandler::createServerErrorException) //
            .bodyToMono(Todo.class)//
            // エクスポネンシャルバックオフによるリトライ
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                .filter(th -> !(th instanceof BusinessException)))
            // サーキットブレーカによる処理
            .transform(it -> cbFactory.create("todo_create").run(it,
                CircuitBreakerErrorFallback.returnMonoBusinessException()))
            .block();
    }

    @Override
    public boolean update(Todo todo) {
        webClient.put().uri(todoByIdUrl(), todo.getTodoId())//
            .headers(this::setAuthHeader)//
            .retrieve()//
            .onStatus(HttpStatusCode::is4xxClientError,
                responseErrorHandler::createClientErrorException)//
            .onStatus(HttpStatusCode::is5xxServerError,
                responseErrorHandler::createServerErrorException) //
            .bodyToMono(Todo.class)//
            // エクスポネンシャルバックオフによるリトライ
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                .filter(th -> !(th instanceof BusinessException)))
            // サーキットブレーカによる処理
            .transform(it -> cbFactory.create("todo_update").run(it,
                CircuitBreakerErrorFallback.returnMonoBusinessException()))
            .block();
        return true;
    }

    @Override
    public boolean delete(Todo todo) {
        webClient.delete().uri(todoByIdUrl(), todo.getTodoId())//
            .headers(this::setAuthHeader)//
            .retrieve()//
            .onStatus(HttpStatusCode::is4xxClientError,
                responseErrorHandler::createClientErrorException)//
            .onStatus(HttpStatusCode::is5xxServerError,
                responseErrorHandler::createServerErrorException) //
            .bodyToMono(Void.class)//
            // エクスポネンシャルバックオフによるリトライ
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                .filter(th -> !(th instanceof BusinessException)))
            // サーキットブレーカによる処理
            .transform(it -> cbFactory.create("todo_delete").run(it,
                CircuitBreakerErrorFallback.returnMonoBusinessException()))
            .block();
        return true;
    }
}
