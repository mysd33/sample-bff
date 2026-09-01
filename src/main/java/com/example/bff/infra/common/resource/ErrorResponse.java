package com.example.bff.infra.common.resource;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/// REST APIのエラーレスポンスクラス
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    // エラーコード
    private String code;
    // エラーメッセージ
    private String message;
    // エラーメッセージ詳細
    private List<String> details;

}
