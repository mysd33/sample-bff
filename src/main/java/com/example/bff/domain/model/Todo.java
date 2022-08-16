package com.example.bff.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Todoクラス
 */

//WebClient(Webflux）の場合だと、jackson.property-naming-strategyプロパティが有効にならないので
//JsonNamingプロパティを直接指定
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Todo implements Serializable {
	private static final long serialVersionUID = -8221174350955399012L;
	//ID
	private String todoId;
	//タイトル
	private String todoTitle;
	//完了したかどうか
	private boolean finished;
	//作成日時
	private Date createdAt;
}