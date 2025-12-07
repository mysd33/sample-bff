package com.example.fw.common.async.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
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
    @Serial
    private static final long serialVersionUID = -7463515743016612451L;

    // ジョブID
    private String jobId;
    // ジョブパラメータ
    private Map<String, String> parameters;
    // ジョブ実行ID（再実行の場合）
    private Long jobExecutionId;

    // 再実行かどうか（Taskletモデルはリラン、Chunkモデルはリスタート）
    private boolean restart;

    /**
     * ジョブパラメータ文字列を返却する
     * 
     * @return ジョブパラメータの文字列
     */
    public String toParameterString() {
        StringJoiner sj = new StringJoiner(",");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            sj.add("%s=%s".formatted(entry.getKey(), entry.getValue()));
        }
        return sj.toString();
    }
    
    /**
     * ジョブパラメータをProperties形式で返却する
     * @return ジョブパラメータのProperties形式で返却する
     */
    public Properties toParameterProperties() {
        Properties properties = new Properties();
        parameters.forEach(properties::put);        
        return properties;
    }
    

    /**
     * 
     * JobRequestが有効な値かを返却する
     * 
     */
    public boolean isValid() {
        if (restart) {
            // リスタートの場合はジョブ実行IDが指定されていること
            return jobExecutionId != null;
        }
        // 初回実行はジョブが指定されていること
        return jobId != null;
    }
}
