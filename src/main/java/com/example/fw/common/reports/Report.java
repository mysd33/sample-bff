package com.example.fw.common.reports;

import java.io.InputStream;

/**
 * 帳票インタフェース
 */
public interface Report {
    // InputStreamデータ
    InputStream getInputStream();
    // データサイズ
    long getSize(); 

}
