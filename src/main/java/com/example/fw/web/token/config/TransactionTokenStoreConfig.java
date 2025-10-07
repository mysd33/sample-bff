package com.example.fw.web.token.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.terasoluna.gfw.web.token.transaction.HttpSessionTransactionTokenStore;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenStore;

import com.example.fw.web.token.MyBatisTransactionTokenStore;

/**
 * 
 * トランザクショントークンチェックのTransactionTokenStoreの設定クラス
 *
 */
@Configuration
@ConditionalOnProperty(prefix = TransactionTokenConfigurationProperties.PROPERTY_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransactionTokenStoreConfig implements WebMvcConfigurer {

    /**
     * TransationTokenStoreクラスの設定（RDBのテーブル管理するTransationTokenStoreに差し替え
     * 
     */
    @ConditionalOnProperty(prefix = TransactionTokenConfigurationProperties.PROPERTY_PREFIX, name = "store-type", havingValue = "db", matchIfMissing = true)
    @Bean
    TransactionTokenStore transactionTokenDBStore() {
        return new MyBatisTransactionTokenStore();
    }

    /**
     * TransationTokenStoreクラスの設定（Sessionの管理するTransationTokenStoreに差し替え
     * 
     */
    @ConditionalOnProperty(prefix = TransactionTokenConfigurationProperties.PROPERTY_PREFIX, name = "store-type", havingValue = "session")
    @Bean
    TransactionTokenStore transactionTokenSessionStore() {
        return new HttpSessionTransactionTokenStore();
    }

}