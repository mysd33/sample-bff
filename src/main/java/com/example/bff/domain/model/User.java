package com.example.bff.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.example.bff.domain.rule.CalendarRules;

//import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
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
//MapStructでのadminプロパティのコピーがうまくいかないので@Builderは入れないこと
public class User implements Serializable {
	private static final String ROLE_GENERAL = "ROLE_GENERAL";

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private static final long serialVersionUID = -8506834435303865959L;

	private String userId;
	// @JsonIgnore
	private String password;
	private String userName;
	private Date birthday;
	private String role;

	public int getAge() {
		return CalendarRules.calcAge(birthday);
	}
	
	public void setAdmin(boolean admin) {
		setRole(admin ? ROLE_ADMIN : ROLE_GENERAL);
	}
	
	public boolean isAdmin() {
		return ROLE_ADMIN.equals(role);
	}

}
