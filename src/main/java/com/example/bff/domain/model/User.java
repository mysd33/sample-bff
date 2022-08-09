package com.example.bff.domain.model;

import java.io.Serializable;
import java.util.Date;

//import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * ユーザクラス
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
	private static final long serialVersionUID = -8506834435303865959L;

	private String userId;
	//@JsonIgnore
	private String password;
	private String userName;
	private Date birthday;
	private String role;
	
    //TODO: サンプルAPの残骸なのでいずれ削除
	//@JsonIgnore	
	private boolean marriage; //結婚ステータス
	//@JsonIgnore	
    private int age; //年齢

}
