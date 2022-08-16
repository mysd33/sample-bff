package com.example.bff.infra.repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.model.TodoList;
import com.example.bff.domain.repository.TodoRepository;
import com.example.fw.common.exception.SystemException;
import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.FrameworkMessageIds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
 */
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClient implements TodoRepository {
	private final WebClientLoggingFilter webClientLoggingFilter;

	@Value("${api.backend.url}/api/v1/todos")
	private String urlTodos;

	@Value("${api.backend.url}/api/v1/todos/{todoId}")
	private String urlTodoById;

	// https://news.mynavi.jp/techplus/article/techp5348/
	// https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
	// https://spring.pleiades.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
	// TODO: ステータス400（業務エラー）やステータス500（システムエラー）の対応

	@Override
	public Optional<Todo> findById(String todoId) {
		Mono<Todo> todoMono = WebClient.builder().filter(webClientLoggingFilter.logFilter()).build().get()
				.uri(urlTodoById, todoId).retrieve().bodyToMono(Todo.class);
		return todoMono.blockOptional();
	}

	@Override
	public Collection<Todo> findAll() {
		Mono<TodoList> todoListMono = WebClient.builder().filter(webClientLoggingFilter.logFilter()).build().get()
				.uri(urlTodos).retrieve().bodyToMono(TodoList.class);
		TodoList list = todoListMono.block();
		return list;
	}

	@Override
	public void create(Todo todo) {
		WebClient.builder().filter(webClientLoggingFilter.logFilter()).build().post().uri(urlTodos)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(todo).retrieve().bodyToMono(Todo.class).block();
	}

	@Override
	public boolean update(Todo todo) {
		WebClient.builder().filter(webClientLoggingFilter.logFilter()).build().put().uri(urlTodoById, todo.getTodoId())
				.retrieve().bodyToMono(Todo.class).block();
		return true;
	}

	@Override
	public void delete(Todo todo) {
		WebClient.builder().filter(webClientLoggingFilter.logFilter()).build().delete().uri(urlTodoById, todo.getTodoId()).retrieve()
				.bodyToMono(Void.class).block();
	}

}