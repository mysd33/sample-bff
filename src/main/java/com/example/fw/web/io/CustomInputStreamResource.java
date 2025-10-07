package com.example.fw.web.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(contentLength);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CustomInputStreamResource other = (CustomInputStreamResource) obj;
        return contentLength == other.contentLength;
    }

}
