package com.example.bff.domain.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Todoクラス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Todo implements Serializable {
    private static final long serialVersionUID = -8221174350955399012L;
    // ID
    private String todoId;
    // タイトル
    private String todoTitle;
    // 完了したかどうか
    private boolean finished;
    // 作成日時
    private Date createdAt;
}