package com.example.bff.infra.repository;

import com.example.bff.domain.model.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface TodoV2ResourceMapper {

    /// モデルからリソースに変換
    TodoV2Resource modelToResource(Todo todo);

    /// リソースからモデルに変換
    @Mapping(target = "userId", ignore = true)
    Todo resourceToModel(TodoV2Resource todoResource);

}
