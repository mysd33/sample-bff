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
@ConditionalOnProperty(prefix = "transaction-token", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransactionTokenStoreConfig implements WebMvcConfigurer {
   
    /**
     * TransationTokenStoreクラスの設定（RDBのテーブル管理するTransationTokenStoreに差し替え
     * 
     */
    @ConditionalOnProperty(prefix = "transaction-token", name = "store-type", havingValue = "db", matchIfMissing = true)
    @Bean
    public TransactionTokenStore transactionTokenDBStore() {
        return new MyBatisTransactionTokenStore();
    }

    
    /**
     * TransationTokenStoreクラスの設定（Sessionの管理するTransationTokenStoreに差し替え
     * 
     */
    @ConditionalOnProperty(prefix = "transaction-token", name = "store-type", havingValue = "session")
    @Bean
    public TransactionTokenStore transactionTokenSessionStore() {
        return new HttpSessionTransactionTokenStore();
    }
    

}