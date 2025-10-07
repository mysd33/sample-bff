package com.example.fw.common.objectstorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.fw.common.constants.FrameworkConstants;

import lombok.Data;

/**
 * オブジェクトストレージアクセス機能のプロパティクラス
 *
 */
@Data
@ConfigurationProperties(prefix = S3ConfigurationProperties.PROPERTY_PREFIX)
public class S3ConfigurationProperties {
    // オブジェクトストレージアクセス機能のプロパティ名のプレフィックス
    static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "aws.s3";
    static final String LOCALFAKE_PROPERTY_PREFIX = PROPERTY_PREFIX + ".localfake";
    // バケット名
    private String bucket;
    // リージョン
    private String region = "ap-northeast-1";
    // S3のローカルFakeの設定
    private S3LocalFakeProperties localfake;

    @Data
    public static class S3LocalFakeProperties {
        // Fakeの種類
        private String type;
        // typeがfileのときのベースディレクトリ
        private String baseDir;
        // typeがs3rver、minioのときのローカルサーバのポート
        private int port = 4568;
        // typeがminioのときのクレデンシャル
        private String accessKeyId = "";
        private String secretAccessKey = "";

    }

}