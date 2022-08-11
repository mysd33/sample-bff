package com.example.bff.domain.service;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;
import com.example.fw.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

/**
 * TodoServiceの実装クラス
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
	private static final long MAX_UNFINISHED_COUNT = 5;

	private final TodoRepository todoRepository;

	@Override
	@Transactional(readOnly = true)
	public Collection<Todo> findAll() {
		return todoRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Todo findOne(String todoId) {		
		return todoRepository.findById(todoId).orElseThrow(() -> {
			
			//TODO:Rest 呼び出し化した場合でのエラー電文対応
			// 対象Todoがない場合、業務エラー
			return new BusinessException(MessageIds.W_EX_2001);
		});
	}

	@Override
	public Todo create(Todo todo) {
		long unfinishedCount = todoRepository.countByFinished(false);
		
		//TODO:Rest 呼び出し化した場合でのエラー電文対応
		if (unfinishedCount >= MAX_UNFINISHED_COUNT) {
			// 未完了のTodoが、5件以上の場合、業務エラー
			throw new BusinessException(MessageIds.W_EX_2002, MAX_UNFINISHED_COUNT);
		}

		String todoId = UUID.randomUUID().toString();
		Date createdAt = new Date();
		todo.setTodoId(todoId);
		todo.setCreatedAt(createdAt);
		todo.setFinished(false);
		todoRepository.create(todo);

		return todo;
	}

	@Override
	public Todo finish(String todoId) {
		Todo todo = findOne(todoId);
		
		//TODO:Rest 呼び出し化した場合でのエラー電文対応
		if (todo.isFinished()) {
			// すでに終了している場合、業務エラー
			throw new BusinessException(MessageIds.W_EX_2003, todoId);
		}
		todo.setFinished(true);
		todoRepository.update(todo);
		return todo;
	}

	@Override
	public void delete(String todoId) {
		Todo todo = findOne(todoId);
		todoRepository.delete(todo);
	}

}