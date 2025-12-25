package com.example.fw.common.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.fw.common.constants.FrameworkConstants;

import lombok.Data;

/**
 * 一時ファイル管理機能に関する設定のプロパティクラス
 */
@Data
@ConfigurationProperties(FileConfigurationProperties.PROPERTY_PREFIX)
public class FileConfigurationProperties {
    // 電子署名の設定を保持するプロパティのプレフィックス
    static final String PROPERTY_PREFIX = FrameworkConstants.PROPERTY_BASE_NAME + "file";
    // 一時ファイルの有効期間のデフォルト値：60秒（1分）
    private static final int DEFAULT_EXPIRATION_SECONDS = 60;
    // 一時ファイルを削除するスケジュールの初期遅延時間のプロパティ。 デフォルト120秒（2分）
    public static final String FILE_DELETE_TEMPFILES_INITIAL_DELAY_SECONDS_PROPERTY = "${example.file.delete-initial-delay-seconds:120}";
    // 一時ファイルを削除するスケジュールの実行間隔のプロパティ。デフォルト60秒（1分）
    public static final String FILE_DELETE_TEMPFILES_FIXED_RATE_SECONDS_PROPERTY = "${example.file.delete-fixed-rate-seconds:60}";
    /**
     * 一時ファイル保存のベースディレクトリ <br>
     * システムプロパティ"java.io.tmpdir"の指すディレクトリ配下に作成されるディレクトリ名
     */
    private String baseDir = "tmpFilesDir";

    /**
     * 一時ファイルの有効期間。更新日時から何秒経過していたら削除対象とするか
     */
    private int expirationSeconds = DEFAULT_EXPIRATION_SECONDS;
}
