package com.example.fw.common.objectstorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * S3のプロパティクラス
 *
 */
@Data
@ConfigurationProperties(prefix = "aws.s3")
public class S3ConfigurationProperties {
    // バケット名
    private String bucket;
    // リージョン名
    private String region = "ap-northeast-1";
    private S3LocalFakeProperties localfake;
    
    @Data
    public static class S3LocalFakeProperties {
        // Fakeの種類
        private String type;
        // typeがfileのときのベースディレクトリ
        private String baseDir;
        // typeがs3rverのときのローカルサーバのポート
        private int port = 4568;
    }
    
}