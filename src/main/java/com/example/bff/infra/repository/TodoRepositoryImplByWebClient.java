package com.example.bff.infra.repository;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.model.TodoList;
import com.example.bff.domain.repository.TodoRepository;
import com.example.bff.infra.common.httpclient.CircutiBreakerErrorFallback;
import com.example.bff.infra.common.httpclient.WebClientResponseErrorHandler;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
 */
@XRayEnabled
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClient implements TodoRepository {
    private final WebClient webClient;
    private final WebClientResponseErrorHandler responseErrorHandler;

    // サーキットブレーカ
    // （参考）https://spring.io/projects/spring-cloud-circuitbreaker
    @SuppressWarnings("rawtypes")
    private final ReactiveCircuitBreakerFactory cbFactory;
    // リトライ回数
    @Value("${api.retry.max-attempts:3}")
    int maxAttempts;
    // エクスポネンシャルバックオフによる初回待機時間
    @Value("${api.retry.min-backoff:200}")
    long minBackoff;

    @Value("${api.backend.url}/api/v1/todos")
    private String urlTodos;

    @Value("${api.backend.url}/api/v1/todos/{todoId}")
    private String urlTodoById;

    // WebClient(WebFlux)版の実装の参考ページ
    // https://news.mynavi.jp/techplus/article/techp5348/
    // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
    // https://spring.pleiades.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
    // https://medium.com/a-developers-odyssey/spring-web-client-exception-handling-cd93cf05b76

    @Override
    public Optional<Todo> findById(String todoId) {
        // @formatter:off 
		Mono<Todo> todoMono = webClient.get()
				.uri(urlTodoById, todoId)				
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,  
				        responseErrorHandler::createClientErrorException
				)
				.onStatus(HttpStatusCode::is5xxServerError,
				        responseErrorHandler::createServerErrorException
				) 
				.bodyToMono(Todo.class)
                // エクスポネンシャルバックオフによるリトライ
                .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff)))
                // サーキットブレーカによる処理
				.transform(it -> cbFactory.create("todo_findById")
						.run(it, CircutiBreakerErrorFallback.returnMonoBusinessException()));
		return todoMono.blockOptional();
		// @formatter:on
    }

    @Override
    public Collection<Todo> findAll() {
        // @formatter:off 

        Mono<TodoList> todoListMono = webClient.get().uri(urlTodos)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,
				        responseErrorHandler::createClientErrorException
				)
				.onStatus(HttpStatusCode::is5xxServerError,
				        responseErrorHandler::createServerErrorException
				) 
				.bodyToMono(TodoList.class)
				// エクスポネンシャルバックオフによるリトライ
				.retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff)))
                // サーキットブレーカによる処理				
				// Fallback時にエラーとせずに空のリストを例
				.transform(it -> cbFactory.create("todo_findAll")
						.run(it, throwable -> Mono.just(new TodoList())));
		// @formatter:on 
        return todoListMono.block();
    }

    @Override
    public void create(Todo todo) {
        // @formatter:off 
		webClient.post().uri(urlTodos)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(todo)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,
				        responseErrorHandler::createClientErrorException
				)
				.onStatus(HttpStatusCode::is5xxServerError,
				        responseErrorHandler::createServerErrorException
				) 
				.bodyToMono(Todo.class)
                // エクスポネンシャルバックオフによるリトライ
                .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff)))
                // サーキットブレーカによる処理                
				.transform(it -> cbFactory.create("todo_create").run(it,
						CircutiBreakerErrorFallback.returnMonoBusinessException()))
				.block();
		// @formatter:on
    }

    @Override
    public boolean update(Todo todo) {
        // @formatter:off 
	    webClient.put().uri(urlTodoById, todo.getTodoId())
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, 
				        responseErrorHandler::createClientErrorException
				)
				.onStatus(HttpStatusCode::is5xxServerError,
				        responseErrorHandler::createServerErrorException
				) 
				.bodyToMono(Todo.class)
                // エクスポネンシャルバックオフによるリトライ
                .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff)))
                // サーキットブレーカによる処理                
				.transform(it -> cbFactory.create("todo_update").run(it,
						CircutiBreakerErrorFallback.returnMonoBusinessException()))
				.block();
	    // @formatter:on 
        return true;
    }

    @Override
    public void delete(Todo todo) {
        // @formatter:off 	    
		webClient.delete().uri(urlTodoById, todo.getTodoId())
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,
				        responseErrorHandler::createClientErrorException
				)
				.onStatus(HttpStatusCode::is5xxServerError,
				        responseErrorHandler::createServerErrorException
				)  
				.bodyToMono(Void.class)
                // エクスポネンシャルバックオフによるリトライ
                .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff)))
                // サーキットブレーカによる処理
				.transform(it -> cbFactory.create("todo_delete").run(it,
						CircutiBreakerErrorFallback.returnMonoBusinessException()))
				.block();
		// @formatter:on 		
    }

}