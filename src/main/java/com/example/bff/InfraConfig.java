package com.example.bff;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

import com.example.bff.domain.repository.RepositoryPackage;
import com.example.bff.infra.common.httpclient.WebClientResponseErrorHandler;
import com.example.fw.common.async.config.SQSCommonConfigPackage;
import com.example.fw.common.async.repository.JobRequestRepository;
import com.example.fw.common.async.repository.JobRequestRepositoryImpl;
import com.example.fw.common.datasource.config.DynamicRoutingDataSourceConfig;
import com.example.fw.common.httpclient.config.WebClientConfigPackage;
import com.example.fw.common.objectstorage.config.S3ConfigPackage;
import com.example.fw.web.token.TransactionTokenPackage;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
@ComponentScan(basePackageClasses = { WebClientConfigPackage.class, S3ConfigPackage.class,
        SQSCommonConfigPackage.class })
//　トランザクショントークンチェックのMyBatisのMapperをスキャンさせるために、業務APのMapper含めて明示的にスキャンする設定を追加
@MapperScan(basePackageClasses = { TransactionTokenPackage.class,
        RepositoryPackage.class }, annotationClass = Mapper.class)
// 動的ルーティングによるデータソース設定を追加
@Import(DynamicRoutingDataSourceConfig.class)
public class InfraConfig {
    @Value("${delayed.batch.queue}")
    private String queueName;

    /**
     * WebClientでのエラーハンドラークラス
     */
    @Bean
    WebClientResponseErrorHandler webClientResponseErrorHandler() {
        return new WebClientResponseErrorHandler();
    }

    /**
     * RestTemplateの設定
     */
//    @Bean
//    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
//        // TODO: X-Rayのトレーシング設定
//        // ログ出力クラスの設定
//        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//        interceptors.add(new RestTemplateLoggingInterceptor());
//        return restTemplateBuilder
//                // エラーハンドラークラスの設定
//                .errorHandler(new RestTemplateResponseErrorHandler()).interceptors(interceptors).build();
//    }

    /**
     * JobRequestRepository（非同期実行）の設定
     * 
     * @param jmsTemplate
     */
    @Bean
    JobRequestRepository jobRequestRepository(JmsTemplate jmsTemplate) {
        return new JobRequestRepositoryImpl(jmsTemplate, queueName);
    }

}
