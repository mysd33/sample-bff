package com.example.fw.common.digitalsignature.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = DigitalSignatureConfigurationProperties.DIGITAL_SIGNATURE)
public class DigitalSignatureConfigurationProperties {
    // 電子署名の設定を保持するプロパティのプレフィックス
    static final String DIGITAL_SIGNATURE = "digitalsignature";
    // 署名に使用するハッシュアルゴリズム（デフォルト: SHA256） PKCS12BasicReportSignerでのみ使用
    private String hashAlgorithm = "SHA256";
    // PKCS#12固有の設定(digitalsignature.pkcs12.*)
    private PKCS12Properties pkcs12 = new PKCS12Properties();
    // AWS KMS固有の設定(digitalsignature.aws-kms.*)
    private AWSKmsProperties awsKms = new AWSKmsProperties();

    /**
     * PKCS#12キーストア固有の設定を保持する内部クラス
     */
    @Data
    public static class PKCS12Properties {
        // キーストアファイルのパス
        private String keystoreFilePath = "";
        // キーストアのパスワード
        private String password = "";
    }

    /**
     * AWS KMS固有の設定を保持する内部クラス
     */
    @Data
    public static class AWSKmsProperties {
        // 署名に使用するAWS KMSのキーID
        // 指定がある場合、キーエイリアスより優先される
        private String keyId;
        // 署名に使用するAWS KMSのキーエリアス（alias/は除く）
        // キーIDが指定されていない場合に使用される
        private String keyAlias;
    }
}
