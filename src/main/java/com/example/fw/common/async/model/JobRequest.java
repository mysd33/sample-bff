package com.example.fw.common.async.model;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

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

	// ジョブID
	private String jobId;
	// ジョブパラメータ
	private Map<String, String> parameters;

	/**
	 * パラメータ文字列を返却する
	 * 
	 * @return パラメータ文字列
	 */
	public String toParameterString() {
		StringJoiner sj = new StringJoiner(",");
		for (String key : parameters.keySet()) {
			sj.add(String.format("%s=%s", key, parameters.get(key)));
		}
		return sj.toString();
	}

}
