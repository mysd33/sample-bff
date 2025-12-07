package com.example.bff.app.web.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * 
 * ユーザ情報のCSVファイルデータクラス
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 定義されていないプロパティを無視してマッピングする
@JsonPropertyOrder({ "ユーザーID", "氏名", "誕生日", "ロール" }) // CSVのヘッダ順
@Data
public class UserCsv implements Serializable {

    @Serial
    private static final long serialVersionUID = -1883999589975469540L;

    @JsonProperty("ユーザーID")
    private String userId; // ユーザーID

    @JsonProperty("氏名")
    private String userName; // ユーザー名

    @JsonProperty("誕生日")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date birthday; // 誕生日

    @JsonProperty("ロール")
    private String role; // ロール

}