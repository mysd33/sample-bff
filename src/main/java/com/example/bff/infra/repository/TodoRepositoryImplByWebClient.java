package com.example.bff.infra.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.model.TodoList;
import com.example.bff.domain.repository.TodoRepository;
import com.example.fw.common.httpclient.WebClientLoggingFilter;
import com.example.fw.common.httpclient.WebClientResponseErrorHandler;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * TodoRepositoryの実装 BackendサービスのREST APIを呼び出す WebFlux実装
 */
@Repository
@RequiredArgsConstructor
public class TodoRepositoryImplByWebClient implements TodoRepository {
	private final WebClientLoggingFilter loggingFilter;
	private final WebClientResponseErrorHandler responseErrorHandler;

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
		Mono<Todo> todoMono = WebClient.builder().filter(loggingFilter.filter()).build().get()
				.uri(urlTodoById, todoId)				
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,  response -> {
					return  responseErrorHandler.createClientErrorException(response);
				})
				.onStatus(HttpStatus::is5xxServerError,response -> {
					return  responseErrorHandler.createServerErrorException(response);
				}) 
				.bodyToMono(Todo.class);				
				
		return todoMono.blockOptional();
	}

	@Override
	public Collection<Todo> findAll() {
		Mono<TodoList> todoListMono = WebClient.builder().filter(loggingFilter.filter()).build()
				.get().uri(urlTodos)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,  response -> {
					return  responseErrorHandler.createClientErrorException(response);
				})
				.onStatus(HttpStatus::is5xxServerError,response -> {
					return  responseErrorHandler.createServerErrorException(response);
				}) 
				.bodyToMono(TodoList.class);
		TodoList list = todoListMono.block();
		return list;
	}

	@Override
	public void create(Todo todo) {
		WebClient.builder().filter(loggingFilter.filter()).build()
				.post().uri(urlTodos)
				.contentType(MediaType.APPLICATION_JSON).bodyValue(todo)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,  response -> {
					return  responseErrorHandler.createClientErrorException(response);
				})
				.onStatus(HttpStatus::is5xxServerError,response -> {
					return  responseErrorHandler.createServerErrorException(response);
				}) 
				.bodyToMono(Todo.class).block();
	}

	@Override
	public boolean update(Todo todo) {
		WebClient.builder().filter(loggingFilter.filter()).build()
				.put().uri(urlTodoById, todo.getTodoId())
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,  response -> {
					return  responseErrorHandler.createClientErrorException(response);
				})
				.onStatus(HttpStatus::is5xxServerError,response -> {
					return  responseErrorHandler.createServerErrorException(response);
				}) 
				.bodyToMono(Todo.class).block();
		return true;
	}

	@Override
	public void delete(Todo todo) {
		WebClient.builder().filter(loggingFilter.filter()).build()
				.delete().uri(urlTodoById, todo.getTodoId())
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,  response -> {
					return  responseErrorHandler.createClientErrorException(response);
				})
				.onStatus(HttpStatus::is5xxServerError,response -> {
					return  responseErrorHandler.createServerErrorException(response);
				})  
				.bodyToMono(Void.class).block();
	}

}