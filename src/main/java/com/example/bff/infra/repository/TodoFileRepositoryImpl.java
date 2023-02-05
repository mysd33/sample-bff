package com.example.bff.infra.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.bff.domain.model.TodoFile;
import com.example.bff.domain.repository.TodoFileRepository;
import com.example.fw.common.file.ObjectStorageFileAccessor;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TodoFileRepositoryImpl implements TodoFileRepository {
    private final ObjectStorageFileAccessor objectStorageFileAccessor;

    @Override
    public void save(TodoFile todoFile) {
        String filePath = UUID.randomUUID().toString() + ".csv"; 
        objectStorageFileAccessor.save(todoFile.getFileInputStream(), filePath);

    }

}
