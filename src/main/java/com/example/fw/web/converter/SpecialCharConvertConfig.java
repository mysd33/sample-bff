
package com.example.fw.web.converter;

import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 特殊文字のコードポイント変換の設定クラス
 */
@Configuration
public class SpecialCharConvertConfig {

    /**
     * 特殊文字のコードポイント変換フィルタ
     * 
     */
    @Bean
    @FilterRegistration
    SpecialCharConvertFilter specialCharConvertFilter() {
        return new SpecialCharConvertFilter();
    }

}
