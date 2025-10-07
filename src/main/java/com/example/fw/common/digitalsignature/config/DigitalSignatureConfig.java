package com.example.fw.common.digitalsignature.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.fw.common.digitalsignature.ReportSigner;
import com.example.fw.common.digitalsignature.basic.PKCS12BasicReportSigner;
import com.example.fw.common.digitalsignature.pades.AWSKmsPAdESReportSigner;
import com.example.fw.common.digitalsignature.pades.PKCS12PAdESReportSiginer;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.objectstorage.ObjectStorageFileAccessor;
import com.example.fw.common.reports.config.ReportsConfigurationProperties;

import lombok.RequiredArgsConstructor;

/**
 * 電子署名に関する設定を定義するクラス
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DigitalSignatureConfigurationProperties.class)
public class DigitalSignatureConfig {
    private static final String DIGITAL_SIGNATURE_TYPE = //
            DigitalSignatureConfigurationProperties.PROPERTY + ".type";
    private final DigitalSignatureConfigurationProperties digitalSignatureConfigurationProperties;
    private final ReportsConfigurationProperties reportsConfigurationProperties;

    /**
     * PKCS#12ファイルを使用し、通常のPDF署名を付与するReportSignerのBean定義
     */
    @Bean
    @ConditionalOnProperty(name = DIGITAL_SIGNATURE_TYPE, havingValue = "pkcs12-basic")
    ReportSigner reportSignerByPKCS12Basic() {
        return new PKCS12BasicReportSigner(reportsConfigurationProperties, //
                digitalSignatureConfigurationProperties);
    }

    /**
     * PKCS#12ファイルを使用し、PAdES形式でのPDF署名を付与するReportSignerのBean定義 （デフォルト）
     */
    @Bean
    @ConditionalOnProperty(name = DIGITAL_SIGNATURE_TYPE, havingValue = "pkcs12-pades")
    ReportSigner reportSignerByPKCS12() {
        return new PKCS12PAdESReportSiginer(reportsConfigurationProperties, //
                digitalSignatureConfigurationProperties);
    }

    /**
     * AWS KMSによる署名鍵を使用し、PAdES形式でのPDF署名を付与するReportSignerのBean定義
     * 
     * @param kmsAsyncClient          KMS非同期クライアント
     * @param configurationProperties 鍵管理の設定プロパティ
     * @return ReportSignerのインスタンス
     */
    @Bean
    @ConditionalOnProperty(name = DIGITAL_SIGNATURE_TYPE, havingValue = "aws-kms-pades")
    ReportSigner reportSignerByKms(KeyManager keyManager, ObjectStorageFileAccessor objectStorageFileAccessor) {
        return new AWSKmsPAdESReportSigner(keyManager, //
                reportsConfigurationProperties, //
                digitalSignatureConfigurationProperties);

    }

}
