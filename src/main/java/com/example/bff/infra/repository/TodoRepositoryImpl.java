package com.example.bff.infra.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

/**
 * TodoRepositoryの実装 BackendサービスのREST APIを呼び出す
 */
//@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepository {

	// TODO: WebClientに変更
	private final RestTemplate restTemplate;
	// TODO :URLのプロパティ
	@Value("TODO")
	private String uriStr;

	@Override
	public Optional<Todo> findById(String todoId) {
		// TODO: URLの構成 /api/v1/todos/{todoId}
		// TODO REST APIの呼び出し
		Todo Todo = restTemplate.getForObject(uriStr, Todo.class, todoId);

		return null;
	}

	@Override
	public Collection<Todo> findAll() {
		// TODO: URLの構成 /api/v1/todos/{todoId}
		// TODO REST APIの呼び出し
		return null;
	}

	@Override
	public void create(Todo todo) {
		// TODO: URLの構成 /api/v1/todos
		// TODO REST APIの呼び出し
		restTemplate.postForObject(uriStr, todo, Todo.class);
	}

	@Override
	public boolean update(Todo todo) {
		// TODO: URLの構成 /api/v1/todos/{todoId}
		// TODO REST APIの呼び出し
		restTemplate.put(uriStr, todo, new Object[] {});
		return true;
	}

	@Override
	public void delete(Todo todo) {
		// TODO: URLの構成 /api/v1/todos/{todoId}
		// TODO REST APIの呼び出し
		restTemplate.delete(uriStr);
	}

	@Override
	public long countByFinished(boolean finished) {
		// TODO REST APIの呼び出し		
		// TODO 呼び出し先REST APIの作成
		return 0;
	}

}