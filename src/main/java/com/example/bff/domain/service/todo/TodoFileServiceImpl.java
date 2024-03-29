package com.example.bff.domain.service.todo;

import org.springframework.stereotype.Service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.model.TodoFile;
import com.example.bff.domain.repository.TodoFileRepository;

import lombok.RequiredArgsConstructor;

@XRayEnabled
@Service
@RequiredArgsConstructor
public class TodoFileServiceImpl implements TodoFileService {
    // リポジトリ
    private final TodoFileRepository todoFileRepository;
    
    
    @Override
    public void save(TodoFile file) {
        todoFileRepository.save(file);
    }

}
