package com.example.bff.app;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

import com.example.bff.domain.model.User;

/**
 * 
 * MapStructを使ったUserのマッパークラス
 *
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * ModelからFormに変換
     * 
     * @param user Model
     * @return Form
     */
    UserForm modelToForm(User user);

    /**
     * FormからModelに変換
     * 
     * @param form Form
     * @return Model
     */
    @Mapping(target = "role", ignore = true)
    User formToModel(UserForm form);

    /**
     * ModelからCSV出力用格納データ(UserCsv)に変換
     * 
     * @param user Model
     * @return UserCsv
     */
    UserCsv modelToCsv(User user);

}
