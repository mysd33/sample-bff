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
    String keyId;
    // キーの状態
    String state;
}
