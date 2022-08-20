package com.example.fw.common.async.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * ジョブの要求情報を管理するクラス
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest implements Serializable {

	private static final long serialVersionUID = -7463515743016612451L;

	// ジョブID（初回実行の場合）
	private String jobId;
	// ジョブパラメータ1（初回実行の場合）
	private String param01;
	// ジョブパラメータ2（初回実行の場合）
	private String param02;
	
	/**
	 * パラメータ文字列を返却する
	 * @return
	 */
	public String toParameterString() {
		return new StringBuilder()
				.append("param01=")
				.append(param01)
				.append(",")
				.append("param02=")
				.append(param02)
				.toString();
	}
	
}
