package com.example.fw.common.keymanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import software.amazon.awssdk.services.kms.model.KeySpec;
import software.amazon.awssdk.services.kms.model.KeyUsageType;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

/**
 * 鍵管理機能の設定を保持するプロパティクラス
 */
@Data
@ConfigurationProperties(prefix = "keymanagement")
public class KeyManagementConfigurationProperties {
    // java.securityのKeyFactoryで指定するのアルゴリズム（デフォルト: EC = 楕円曲線暗号）
    // 参考：https://docs.oracle.com/javase/jp/21/docs/specs/security/standard-names.html
    private String keyFactoryAlgorithm = "EC";
    // 署名アルゴリズム（デフォルト： SHA256WITHECDSA = SHA-256ハッシュとECDSA署名の組み合わせ）
    private String signatureAlgorithm = "SHA256WITHECDSA";
    // 署名のハッシュアルゴリズム名（デフォルト：SHAR-256）
    private String hashAlgorithm = "SHA-256";
    // 証明書のPEMファイルのオブジェクトストレージ配置先のベースプレフィックス
    private String certsBasePrefix = "certs/";
    // CSR（証明書署名要求）のPEMファイル名
    private String csrPemFileName = "csr.pem";
    // 自己署名証明書のPEMファイル名
    private String selfSignedCertPemFileName = "self-certificate.pem";
    // 自己署名証明書作成時の有効期間（デフォルト：365日）
    private int selfSignedCertValidityDays = 365;
    // 証明書のPEMファイル名
    private String certPemFileName = "certificate.pem";
    
    // AWS KMS固有の設定(keymanagement.aws-kms.*)
    private AWSKmsProperties awsKms = new AWSKmsProperties();

    /**
     * AWS KMS固有の設定を保持する内部クラス
     */
    @Data
    public static class AWSKmsProperties {
        // KMSのリージョン
        private String region = "ap-northeast-1";
        // 生成するKMSのキー仕様（デフォルト：ECC_NIST_P256）
        // https://docs.aws.amazon.com/ja_jp/kms/latest/developerguide/symm-asymm-choose-key-spec.html#key-spec-ecc
        private KeySpec keySpec = KeySpec.ECC_NIST_P256;
        // KMSのキーの使用タイプ（デフォルト：SIGN_VERIFY）
        private KeyUsageType keyUsage = KeyUsageType.SIGN_VERIFY;
        // KMSのキーの説明
        private String keyDescription = "電子署名用の暗号鍵";
        // KMSの署名アルゴリズム名（デフォルト：ECDSA_SHA_256）
        private SigningAlgorithmSpec kmsSigningAlgorithmSpec = SigningAlgorithmSpec.ECDSA_SHA_256;
        // KMSキー削除時の猶予期限（デフォルト：7日）
        private int pendingDeleteWindowInDays = 7;
    }

}
