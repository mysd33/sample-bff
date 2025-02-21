package com.example.fw.web.io;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

/**
 * レスポンスデータを作成するためのユーティリティメソッドクラス
 */
public class ResponseUtil {
    private ResponseUtil() {
    }

    /**
     * PDFファイル用のレスポンスを生成する（InputStream版）
     * 
     * @param inputStream ダウンロードするPDFファイルの入力ストリーム
     * @param fileName    ファイル名
     * @fileSize ファイルサイズ
     * @return レスポンスデータ
     */
    public static ResponseEntity<Resource> createResponseForPDF(final InputStream inputStream, final String fileName,
            final long fileSize) {
        Assert.notNull(inputStream, "inputStreamがnullです。");
        Assert.hasText(fileName, "fileNameが空です。");
        Assert.isTrue(fileSize > 0, "fileSizeは0より大きい値である必要があります。");
        // CustomInputStreamResourceを使用
        Resource reportResource = new CustomInputStreamResource(inputStream, fileSize);
        return createResponse(MediaType.APPLICATION_PDF, reportResource, fileName);
    }

    /**
     * PDFファイル用のレスポンスを生成する（File版）
     * 
     * @param file ダウンロードするPDFファイル
     * @return レスポンスデータ
     */
    public static ResponseEntity<Resource> createResponseForPDF(final File file) {
        Assert.notNull(file, "fileがnullです。");
        Assert.isTrue(file.exists(), "fileが存在しません。");
        // FileSystemResourceを使用
        Resource reportResource = new FileSystemResource(file);
        return createResponse(MediaType.APPLICATION_PDF, reportResource, file.getName());
    }

    /**
     * PDFファイル用のレスポンスを生成する（Path版）
     * 
     * @param filePath ダウンロードするPDFファイルのパス
     * @return レスポンスデータ
     */
    public static ResponseEntity<Resource> createResponseForPDF(final Path filePath) {
        Assert.notNull(filePath, "filePathがnullです。");
        Assert.isTrue(Files.exists(filePath), "filePathが存在しません。");
        // FileSystemResourceを使用
        Resource reportResource = new FileSystemResource(filePath);
        return createResponse(MediaType.APPLICATION_PDF, reportResource, filePath.getFileName().toString());
    }

    /**
     * ダウンロードファイル用のレスポンスを生成する
     * 
     * @param mediaType メディアタイプ
     * @param resource  ダウンロードするファイルのリソース
     * @param fileName  ファイル名
     * @fileSize ファイルサイズ
     * @return レスポンスデータ
     */
    private static ResponseEntity<Resource> createResponse(final MediaType mediaType, final Resource resource,
            final String fileName) {
        return ResponseEntity.ok()//
                .contentType(mediaType)//
                .cacheControl(CacheControl.noCache())//
                .header(HttpHeaders.CONTENT_DISPOSITION, //
                        ContentDisposition.attachment()//
                                .filename(encodeUtf8(fileName))//
                                .build().toString())//
                .body(resource);
    }

    /**
     * ファイル名をUTF-8でエンコードする
     * 
     * 
     * @param fileName ファイル名
     * @return エンコードされたファイル名
     */
    private static String encodeUtf8(final String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }
}
