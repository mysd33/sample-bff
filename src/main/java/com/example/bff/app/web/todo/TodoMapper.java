package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.example.bff.domain.model.Todo;

/**
 * 
 * MapStructを使ったTodoのマッパークラス
 *
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface TodoMapper {

    /**
     * ModelからFormへ変換する
     * 
     * @param model Model
     * @return Form
     */
    TodoForm modelToForm(Todo model);

    /**
     * FormからModelへ変換する
     * 
     * @param form Form
     * @return Model
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "finished", ignore = true)
    Todo formToModel(TodoForm form);

}
