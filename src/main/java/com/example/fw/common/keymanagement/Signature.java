package com.example.fw.common.keymanagement;

import lombok.Builder;
import lombok.Value;

/**
 * 署名情報を保持するクラス
 */
@Builder
@Value
public class Signature {
    // 署名データ
    private final byte[] value;
}
