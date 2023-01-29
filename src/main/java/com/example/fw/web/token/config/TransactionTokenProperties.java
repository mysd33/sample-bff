package com.example.fw.web.token.config;

import java.util.Collections;
import java.util.List;

import lombok.Data;

/**
 * 
 * トランザクショントークンチェックのプロパティクラス
 *
 */
@Data
public class TransactionTokenProperties {
    //　トランザクショントークン機能の有効化フラグ
    private boolean enabled = true;
    
    // トランザクショントークンチェックを適用するパス
    private List<String> pathPatterns = Collections.emptyList();

    // トランザクショントークンチェックを適用除外するパス
    private List<String> excludePathPatterns = Collections.emptyList();


}