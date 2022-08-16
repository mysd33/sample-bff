package com.example.bff.infra.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.example.bff.domain.model.Todo;
import com.example.bff.domain.model.TodoList;
import com.example.bff.domain.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

/**
 * TodoRepositoryの実装 BackendサービスのREST APIを呼び出す
 */
//WebClient（WebFulx）版のTodoRepository実装を使用しているためコメントアウト
//@Repository  
@RequiredArgsConstructor
public class TodoRepositoryImplByRestTemplate implements TodoRepository {

	private final RestTemplate restTemplate;
	 
	@Value("${api.backend.url}/api/v1/todos")
	private String urlTodos;
	
	@Value("${api.backend.url}/api/v1/todos/{todoId}")
	private String urlTodoById;
	
	@Override
	public Optional<Todo> findById(String todoId) {
		Todo todo = restTemplate.getForObject(urlTodoById, Todo.class, todoId);
		return Optional.ofNullable(todo);		
	}

	@Override
	public Collection<Todo> findAll() {		
		TodoList todoList = restTemplate.getForObject(urlTodos, TodoList.class); 
		return todoList;
	}

	@Override
	public void create(Todo todo) {
		restTemplate.postForObject(urlTodos, todo, Todo.class);
	}

	@Override
	public boolean update(Todo todo) {
		restTemplate.put(urlTodoById, null, todo.getTodoId());
		return true;
	}

	@Override
	public void delete(Todo todo) {
		restTemplate.delete(urlTodoById, todo.getTodoId());
	}

}