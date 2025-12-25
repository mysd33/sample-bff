package com.example.fw.common.file.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.fw.common.file.DefaultTempFileCreator;
import com.example.fw.common.file.TempFileCreator;

/**
 * 一時ファイル管理機能に関する設定を定義するクラス
 */
@Configuration
@EnableConfigurationProperties(FileConfigurationProperties.class)
public class FileConfig {

    /**
     * 一時ファイル作成クラスのBean定義
     * 
     */
    @Bean
    TempFileCreator tempFileCreator(FileConfigurationProperties fileConfigurationProperties) {
        return new DefaultTempFileCreator(fileConfigurationProperties);
    }
}
