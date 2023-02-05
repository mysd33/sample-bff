package com.example.bff.domain.repository;

import com.example.bff.domain.model.TodoFile;

/**
 * Todoファイルを保存するリポジトリインタフェース 
 *
 */
public interface TodoFileRepository {

    /**
     * ファイルを保存する
     * 
     * @param Todoファイルの情報
     * 
     */
    void save(TodoFile todoFile);
}
