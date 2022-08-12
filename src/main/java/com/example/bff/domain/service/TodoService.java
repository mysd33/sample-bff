package com.example.bff.domain.service;

import java.util.Collection;

import com.example.bff.domain.model.Todo;

/**
 * TodoServiceのインタフェース 
 *
 */
public interface TodoService {
	/**
	 * Todoを一件取得する
	 * @param todoId ID
	 * @return Todo
	 */
    Todo findOne(String todoId);
    
    /**
     * Todoを全件取得する
     * @return Todoの全件リスト
     */
    Collection<Todo> findAll();

    /**
     * Todoを作成する
     * @param todo 作成するTodo
     * @return 作成したTodo
     */
    Todo create(Todo todo);

    /**
     * Todoを完了する
     * @param todoId 完了するTodoのID
     * @return 完了したTodo
     */
    void finish(String todoId);

    /**
     * Todoを削除する
     * @param todoId 削除するTodoのID
     */
    void delete(String todoId);
}
