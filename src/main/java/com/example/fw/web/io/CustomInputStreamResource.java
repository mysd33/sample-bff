package com.example.fw.web.io;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

/**
 * InputStreamResourceの拡張クラス
 * 
 * Content-Lengthをあらかじめ指定しておくことで、Content-Lengthヘッダを正しく返すようにする
 */
public class CustomInputStreamResource extends InputStreamResource {
    private final long contentLength;

    /**
     * コンストラクタ
     * 
     * @param inputStream   コンテンツの入力ストリーム
     * @param contentLength コンテンツサイズ
     */
    public CustomInputStreamResource(final InputStream inputStream, final long contentLength) {
        super(inputStream);
        this.contentLength = contentLength;
    }

    /*
     * @see org.springframework.core.io.InputStreamResource#contentLength()
     */
    @Override
    public long contentLength() throws IOException { //
        return contentLength;
    }

}
