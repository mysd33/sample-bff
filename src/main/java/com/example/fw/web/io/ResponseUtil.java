package com.example.fw.web.io;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
	 * PDFファイル用のレスポンスを生成する
	 * 
	 * @param inputStream 帳票の入力ストリーム
	 * @param fileName    ファイル名
	 * @return
	 */
	public static ResponseEntity<Resource> createResponseForPDF(final InputStream inputStream, final String fileName) {
		Resource reportResource = new InputStreamResource(inputStream);
		return ResponseEntity.ok()//
				//.contentType(MediaType.APPLICATION_OCTET_STREAM)//
				.contentType(MediaType.APPLICATION_PDF)//
				.header(HttpHeaders.CONTENT_DISPOSITION, //
						ContentDisposition.attachment()//
								.filename(encodeUtf8(fileName))//								
								.build().toString())//
				.body(reportResource);
	}

	/**
	 * ファイル名をUTF-8でエンコードする
	 * @param filename ファイル名
	 * @return エンコードされたファイル名
	 */
	private static String encodeUtf8(final String filename) {
		String encoded = null;

		try {
			encoded = URLEncoder.encode(filename, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
			// 例外は発生しない
		}

		return encoded;
	}
}
