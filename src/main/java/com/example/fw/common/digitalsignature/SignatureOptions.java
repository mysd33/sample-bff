package com.example.fw.common.digitalsignature;

import java.io.File;

import lombok.Builder;
import lombok.Value;

//TODO: 見直し

@Builder
@Value
public class SignatureOptions {

    /**
     * 可視署名かどうか
     */
    private boolean visible;
    
    /**
     * 署名の画像ファイルパス
     * 
     */
    private String stampImagePath;

    /**
     * キーストアのファイル
     */
    private File keyStoreFile;
    /**
     * パスワード
     */
    private String password;;
}
