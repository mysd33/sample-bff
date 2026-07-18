package com.example.bff.infra.repository;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
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
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/// TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
@XRayEnabled
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClient implements TodoRepository {

    public static final String CLIENT_REGISTRATION_ID = "keycloak";

    //TODO: OIDCありと、無しのWebClientのBeanを2つ用意しないとだめそう
    private final WebClient webClient;
    private final WebClient webClientWithOIDC;
    private final WebClientResponseErrorHandler responseErrorHandler;
    // サーキットブレーカ
    // （参考）https://spring.io/projects/spring-cloud-circuitbreaker
    @SuppressWarnings("rawtypes")
    private final ReactiveCircuitBreakerFactory cbFactory;

    @Nullable
    private final OAuth2AuthorizedClientService authorizedClientService;

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

    @Override
    public Optional<Todo> findById(String todoId) {
        var todoRequestHeaderSpec = webClient().get().uri(todoByIdUrl(), todoId);//
        var todoRequestHeaderSpecWithAuthInfo = addAuthInfo(todoRequestHeaderSpec);
        var todoMono = todoRequestHeaderSpecWithAuthInfo
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
        var todoRequestHeaderSpec = webClient().get().uri(uri.toUri());//
        var todoRequestHeaderSpecWithAuthInfo = addAuthInfo(todoRequestHeaderSpec);
        var todoListMono = todoRequestHeaderSpecWithAuthInfo
            // Spring Security OAuth Clientにアクセストークンを付与してもらうようにする
            .attributes(
                clientRegistrationId(CLIENT_REGISTRATION_ID))//
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
        var todoRequestBodySpec = webClient().post().uri(todosUrl());//
        var todoRequestBodySpecWithAuthInfo = addAuthInfo(todoRequestBodySpec);
        todoRequestBodySpecWithAuthInfo
            // Spring Security OAuth Clientにアクセストークンを付与してもらうようにする
            .attributes(
                clientRegistrationId(CLIENT_REGISTRATION_ID))//
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
        var todoRequestBodySpec = webClient().put().uri(todoByIdUrl(), todo.getTodoId());
        var todoRequestBodySpecWithAuthInfo = addAuthInfo(todoRequestBodySpec);
        todoRequestBodySpecWithAuthInfo
            // Spring Security OAuth Clientにアクセストークンを付与してもらうようにする
            .attributes(
                clientRegistrationId(CLIENT_REGISTRATION_ID))//
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
        var todoRequestHeaderSpec = webClient().delete().uri(todoByIdUrl(), todo.getTodoId());
        var todoRequestHeaderSpecWithAuthInfo = addAuthInfo(todoRequestHeaderSpec);
        todoRequestHeaderSpecWithAuthInfo
            // Spring Security OAuth Clientにアクセストークンを付与してもらうようにする
            .attributes(
                clientRegistrationId(CLIENT_REGISTRATION_ID))//
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

    /// KeyCloakのアクセストークンが存在する場合OIDC/Auth用のWebClient、そうでない場合は普通のWebClientを返す
    private WebClient webClient() {
        return resolveAccessToken() ? webClientWithOIDC : webClient;
    }


    /// KeyCloakのアクセストークンが存在する場合はv2、なければv1のURLを返す
    private String todosUrl() {
        String path = resolveAccessToken() ? "/api/v2/todos" : "/api/v1/todos";
        return UriComponentsBuilder.fromUriString(backendUrl).path(path).build().toUriString();
    }

    /// KeyCloakのアクセストークンが存在する場合はv2、なければv1のURLを返す
    private String todoByIdUrl() {
        String path = resolveAccessToken() ? "/api/v2/todos/{todoId}" : "/api/v1/todos/{todoId}";
        return UriComponentsBuilder.fromUriString(backendUrl).path(path).build().toUriString();
    }

    /// KeyCloakのアクセストークンがあれば、リクエストに付与する
    /// それ以外は、Basic認証で呼び出すようにする
    private @NonNull RequestHeadersSpec<? extends RequestHeadersSpec<?>> addAuthInfo(
        RequestHeadersSpec<? extends RequestHeadersSpec<?>> todoRequestHeaderSpec) {
        return resolveAccessToken() ?
            todoRequestHeaderSpec
                .attributes(clientRegistrationId(CLIENT_REGISTRATION_ID))//
            : todoRequestHeaderSpec.headers(httpHeaders ->
                httpHeaders.setBasicAuth(basicAuthUsername, basicAuthPassword)
            );
    }

    /// KeyCloakアクセストークンがあれば、リクエストに付与する
    /// それ以外は、Basic認証で呼び出すようにする
    private WebClient.RequestBodySpec addAuthInfo(
        RequestBodySpec todoRequestBodySpec) {
        return resolveAccessToken() ?
            todoRequestBodySpec
                .attributes(clientRegistrationId(CLIENT_REGISTRATION_ID))
            : todoRequestBodySpec.headers(httpHeaders ->
                httpHeaders.setBasicAuth(basicAuthUsername, basicAuthPassword)
            );
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
        // サンプルAPではKeyCloakの場合のみアクセストークンを送信するようにする
        // (それ以外のIdPでは対応が難しいのでBasic認証にする)
        if (!CLIENT_REGISTRATION_ID.equals(
            oauth2Authentication.getAuthorizedClientRegistrationId())) {
            return false;
        }
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauth2Authentication.getAuthorizedClientRegistrationId(),
            oauth2Authentication.getName());
        if (authorizedClient == null) {
            return false;
        }
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return accessToken != null && StringUtils.hasText(accessToken.getTokenValue());
    }
}
