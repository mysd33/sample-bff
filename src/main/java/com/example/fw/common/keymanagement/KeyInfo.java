package com.example.fw.common.keymanagement;

import lombok.Builder;
import lombok.Value;

/**
 * 暗号鍵の情報を保持するクラス。
 * 
 */
@Value
@Builder
public class KeyInfo {
    // キーのID
    private final String keyId;
    // キーの状態
    private final String state;
}
