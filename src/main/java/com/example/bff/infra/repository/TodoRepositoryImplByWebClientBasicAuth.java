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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/// TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
@XRayEnabled
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClientBasicAuth implements TodoRepository {

    private final WebClient webClient;
    private final WebClientResponseErrorHandler responseErrorHandler;

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

    @Override
    public Optional<Todo> findById(String todoId) {
        var todoMono = webClient.get().uri(todoByIdUrl(), todoId)
            .headers(headers -> headers.setBasicAuth(basicAuthUsername, basicAuthPassword))
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
        var todoListMono = webClient.get().uri(uri.toUri())
            .headers(headers -> headers.setBasicAuth(basicAuthUsername, basicAuthPassword))
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
            .transform(it -> cbFactory.create("todo_findAllByUserId").run(it,
                _ -> Mono.just(new TodoList())));
        return todoListMono.block();
    }

    @Override
    public void create(Todo todo) {
        webClient.post().uri(todosUrl())
            .headers(headers -> headers.setBasicAuth(basicAuthUsername, basicAuthPassword))
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
        webClient.put().uri(todoByIdUrl(), todo.getTodoId())
            .headers(headers -> headers.setBasicAuth(basicAuthUsername, basicAuthPassword))
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
        webClient.delete().uri(todoByIdUrl(), todo.getTodoId())
            .headers(headers -> headers.setBasicAuth(basicAuthUsername, basicAuthPassword))
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

    private String todosUrl() {
        return UriComponentsBuilder.fromUriString(backendUrl).path("/api/v1/todos").build()
            .toUriString();
    }

    private String todoByIdUrl() {
        return UriComponentsBuilder.fromUriString(backendUrl).path("/api/v1/todos/{todoId}").build()
            .toUriString();
    }

}
