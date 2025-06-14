package com.example.bff.app.web.todo;

import java.io.Serializable;

import com.example.fw.common.validation.CharSet;
import com.example.fw.common.validation.RangeLength;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoForm implements Serializable {
    private static final long serialVersionUID = 6388192822188679641L;

    public static interface TodoCreate {
    }

    public static interface TodoFinish {
    }

    public static interface TodoDelete {
    }

    @NotNull(groups = { TodoFinish.class, TodoDelete.class })
    private String todoId; // ID

    @NotBlank(groups = { TodoCreate.class })
    @RangeLength(min = 1, max = 30, groups = { TodoCreate.class })
    @CharSet(groups = { TodoCreate.class })
    private String todoTitle; // タイトル
}
