package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	UserForm modelToForm(User user);
	
	@Mapping(target ="role", ignore = true)	
	User formToModel(UserForm form);
	
	UserCsv modelToCsv(User user);

}
