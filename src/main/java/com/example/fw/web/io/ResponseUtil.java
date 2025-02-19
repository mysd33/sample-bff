package com.example.fw.web.io;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * レスポンスデータを作成するためのユーティリティメソッドクラス
 */
public class ResponseUtil {
    private ResponseUtil() {
    }

    /**
     * PDFファイル用のレスポンスを生成する（InputStream版）
     * 
     * @param inputStream 帳票の入力ストリーム
     * @param fileName    ファイル名
     * @fileSize ファイルサイズ
     * @return レスポンスデータ
     */
    public static ResponseEntity<Resource> createResponseForPDF(final InputStream inputStream, final String fileName,
            final long fileSize) {
        Resource reportResource = new CustomInputStreamResource(inputStream, fileSize);
        return ResponseEntity.ok()//
                // .contentType(MediaType.APPLICATION_OCTET_STREAM)//
                .contentType(MediaType.APPLICATION_PDF)//
                .contentLength(fileSize)//
                .cacheControl(CacheControl.noCache())//
                .header(HttpHeaders.CONTENT_DISPOSITION, //
                        ContentDisposition.attachment()//
                                .filename(encodeUtf8(fileName))//
                                .build().toString())//
                .body(reportResource);
    }

    /**
     * PDFファイル用のレスポンスを生成する（File版）
     * 
     * @param file 帳票ファイル
     * @return レスポンスデータ
     */
    public static ResponseEntity<Resource> createResponseForPDF(final File file) {
        // FileSystemResourceを使用
        Resource reportResource = new FileSystemResource(file);
        return ResponseEntity.ok()//
                // .contentType(MediaType.APPLICATION_OCTET_STREAM)//
                .contentType(MediaType.APPLICATION_PDF)//
                .cacheControl(CacheControl.noCache())//
                .header(HttpHeaders.CONTENT_DISPOSITION, //
                        ContentDisposition.attachment()//
                                .filename(file.getName())//
                                .build().toString())//
                .body(reportResource);
    }

    /**
     * ファイル名をUTF-8でエンコードする
     * 
     * @param filename ファイル名
     * @return エンコードされたファイル名
     */
    private static String encodeUtf8(final String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8);
    }
}
