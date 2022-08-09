package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.User;

@Mapper
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	UserForm modelToForm(User user);
	User formToModel(UserForm form);

}
