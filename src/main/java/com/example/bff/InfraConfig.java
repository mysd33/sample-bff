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
import com.example.fw.common.httpclient.config.WebClientConfigPackage;
import com.example.fw.common.logging.config.LoggingConfigPackage;
import com.example.fw.common.metrics.config.MetricsConfig;
import com.example.fw.common.objectstorage.config.S3ConfigPackage;
import com.example.fw.common.rdb.config.DynamicRoutingDataSourceConfig;
import com.example.fw.common.reports.config.ReportsConfigPackage;
import com.example.fw.web.token.TransactionTokenPackage;

/**
 * 
 * インフラストラクチャ層の設定クラス
 *
 */
@Configuration
// RESTクライアント、オブジェクトストレージアクセス、非同期処理依頼の設定、
// 帳票出力の設定、ロギング拡張機能の設定を追加
@ComponentScan(basePackageClasses = { WebClientConfigPackage.class, S3ConfigPackage.class, //
        SQSCommonConfigPackage.class, ReportsConfigPackage.class, LoggingConfigPackage.class }) //
// 鍵管理機能とPDF電子署名機能を有効化するには、ComponentScanに以下を追加する
// , KeyManagementConfigPackage.class, DigitalSignatureConfigPackage.class })
//動的ルーティングによるデータソース設定、Micrometerのカスタムメトリックス設定を追加
@Import({ DynamicRoutingDataSourceConfig.class, MetricsConfig.class })
// トランザクショントークンチェックのMyBatisのMapperをスキャンさせるために、業務APのMapper含めて明示的にスキャンする設定を追加
@MapperScan(basePackageClasses = { TransactionTokenPackage.class,
        RepositoryPackage.class }, annotationClass = Mapper.class)
public class InfraConfig {
    @Value("${example.async.queue}")
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
