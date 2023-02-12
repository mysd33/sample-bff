package com.example.bff.infra.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.bff.domain.model.TodoFile;
import com.example.bff.domain.repository.TodoFileRepository;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.UploadObject;

import lombok.RequiredArgsConstructor;

/**
 * Todoファイルを保存するリポジトリクラス
 */
@Repository
@RequiredArgsConstructor
public class TodoFileRepositoryImpl implements TodoFileRepository {
    private static final String TODO_FILES_DIR = "todoFiles/";
    private final ObjectStorageFileAccessor objectStorageFileAccessor;

    @Override
    public void save(TodoFile todoFile) {
        String filePath = TODO_FILES_DIR + UUID.randomUUID().toString() + ".csv";              
        todoFile.setTargetFilePath(filePath);
        
        objectStorageFileAccessor.upload(UploadObject.builder()
                .inputStream(todoFile.getFileInputStream())
                .targetFilePath(filePath)
                .build());  

    }

}
