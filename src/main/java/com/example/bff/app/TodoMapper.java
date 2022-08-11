package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.Todo;

@Mapper
public interface TodoMapper {
	TodoMapper INSTANCE = Mappers.getMapper(TodoMapper.class);
	TodoForm modelToForm(Todo todo);
	Todo formToModel(TodoForm form);

}
