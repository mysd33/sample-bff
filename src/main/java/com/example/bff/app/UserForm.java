package com.example.bff.app;

import java.util.Date;

import javax.validation.GroupSequence;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 
 * ユーザ登録画面・更新画面データクラス
 *
 */
@Data
public class UserForm {
	// 入力チェック順序を制御するためのインタフェース
	@GroupSequence({ ValidGroup1.class, ValidGroup2.class, ValidGroup3.class })
	public static interface GroupOrder {
	}

	public static interface ValidGroup1 {
	}

	public static interface ValidGroup2 {
	}

	public static interface ValidGroup3 {
	}

	@NotBlank(groups = ValidGroup1.class)
	@Email(groups = ValidGroup2.class)
	private String userId; // ユーザーID

	@NotBlank(groups = ValidGroup1.class)
	@Size(min = 4, max = 100, groups = ValidGroup2.class)
	@Pattern(regexp = "^[a-zA-Z0-9]+$", groups = ValidGroup3.class)
	private String password; // パスワード

	@NotBlank(groups = ValidGroup1.class)
	private String userName; // ユーザー名

	@NotNull(groups = ValidGroup1.class)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday; // 誕生日

	@Min(value = 20, groups = ValidGroup2.class)
	@Max(value = 100, groups = ValidGroup2.class)
	private int age; // 年齢

}
