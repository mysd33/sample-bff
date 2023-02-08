package com.example.fw.common.objectstorage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessorFake;

/**
 * 
 * S3が開発環境上でのローカル実行で使用できない場合を考慮した
 * 通常のファイルシステムアクセスのFakeに置き換える設定クラス
 *
 */
@Profile("dev")
@ConditionalOnProperty(prefix = "aws.s3.localfake", name = "type", havingValue = "file", matchIfMissing = true)
@Configuration
public class S3LocalFileFakeConfig {    
   
    /**
     * ローカル実行用のFake
     * 
     */
    @Bean    
    public ObjectStorageFileAccessor objectStorageFileAccessorFake() {
        return new ObjectStorageFileAccessorFake();
    }
    

}
