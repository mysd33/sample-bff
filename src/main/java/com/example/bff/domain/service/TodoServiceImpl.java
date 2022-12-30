package com.example.bff.domain.service;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

/**
 * TodoServiceの実装クラス
 */
@XRayEnabled
@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {	

	private final TodoRepository todoRepository;

	@Override
	@Transactional(readOnly = true)
	public Collection<Todo> findAll() {
		return todoRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Todo findOne(String todoId) {		
		return todoRepository.findById(todoId).orElse(null);
	}

	@Override
	public Todo create(Todo todo) {
		todoRepository.create(todo);

		return todo;
	}

	@Override
	public void finish(String todoId) {
		todoRepository.update(Todo.builder().todoId(todoId).build());		
	}

	@Override
	public void delete(String todoId) {
		Todo todo = findOne(todoId);
		todoRepository.delete(todo);
	}

}