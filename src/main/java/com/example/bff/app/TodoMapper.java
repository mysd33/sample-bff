package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.Todo;

@Mapper(componentModel = "spring")
public interface TodoMapper {
	TodoMapper INSTANCE = Mappers.getMapper(TodoMapper.class);
		
	TodoForm modelToForm(Todo todo);
	
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "finished", ignore = true)
	Todo formToModel(TodoForm form);

}
