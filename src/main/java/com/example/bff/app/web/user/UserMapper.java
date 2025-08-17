package com.example.bff.app.web.user;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.example.bff.domain.model.User;
import com.example.bff.domain.reports.UserListReportItem;

/**
 * 
 * MapStructを使ったUserのマッパークラス
 *
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface UserMapper {

    /**
     * ModelからFormに変換
     * 
     * @param user Model
     * @return Form
     */
    @Mapping(target = "confirmPassword", ignore = true)
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

    /**
     * ModelからPDF帳票用格納データ（UserListReportItem)に変換
     * @param user
     * @return UserListReportItem
     */
    UserListReportItem modelToReportItem(User user);

    /**
     * ModelのリストからCSV出力用格納データ(UserCsv)のリストに変換
     * 
     * @param users Model
     * @return UserCsvのリスト
     */
    default List<UserCsv> modelsToCsvs(List<User> users) {
        return users.stream().map(this::modelToCsv).toList();
    }
    
    /**
     * ModelのリストからPDF帳票用格納データ（UserListReportItem)のリストに変換
     * @param users Model
     * @return UserListReportItemのリスト
     */
    default List<UserListReportItem> modelsToReportItems(List<User> users) {
        return users.stream().map(this::modelToReportItem).toList();
    }
}
