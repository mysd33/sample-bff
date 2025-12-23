package com.example.fw.web.token.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.fw.common.constants.FrameworkConstants;

import lombok.Data;

/**
 * 
 * トランザクショントークンチェックのプロパティクラス
 *
 */
@Data
@ConfigurationProperties(TransactionTokenConfigurationProperties.PROPERTY_PREFIX)
public class TransactionTokenConfigurationProperties {
    // トランザクショントークンチェック機能のプロパティプレフィックス
    static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "transaction-token";
    // トランザクショントークン機能の有効化フラグ
    private boolean enabled = true;

    // トランザクショントークンの保存先（db、session)
    private String storeType;

    // トランザクショントークンチェックを適用するパス
    private List<String> pathPatterns = Collections.emptyList();

    // トランザクショントークンチェックを適用除外するパス
    private List<String> excludePathPatterns = Collections.emptyList();

}