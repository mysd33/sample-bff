package com.example.bff.domain.service.todo;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.message.CommonMessageIds;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.repository.TodoRepository;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TodoServiceの実装クラス
 */
@XRayEnabled
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TodoServiceImpl implements TodoService {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    private final TodoRepository todoRepository;

    @Override
    @Transactional(readOnly = true)
    public Collection<Todo> findAll() {
        appLogger.info(CommonMessageIds.I_CMN_0001);

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