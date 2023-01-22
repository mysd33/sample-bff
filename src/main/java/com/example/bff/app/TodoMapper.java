package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.Todo;

/**
 * 
 * MapStructを使ったTodoのマッパークラス
 *
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface TodoMapper {
    TodoMapper INSTANCE = Mappers.getMapper(TodoMapper.class);

    /**
     * ModelからFormへ変換する
     * 
     * @param todo Model
     * @return Form
     */
    TodoForm modelToForm(Todo todo);

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
