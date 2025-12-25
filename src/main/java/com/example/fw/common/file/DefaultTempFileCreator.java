package com.example.fw.common.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.file.config.FileConfigurationProperties;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTempFileCreator implements TempFileCreator {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    // Javaの一時ディレクトリのパス
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private final FileConfigurationProperties fileConfigurationProperties;

    // 一時ファイルのディレクトリ
    private final Path tempFilesDirPath;

    /**
     * コントラクタ
     */
    public DefaultTempFileCreator(final FileConfigurationProperties fileConfigurationProperties) {
        this.fileConfigurationProperties = fileConfigurationProperties;
        tempFilesDirPath = Path.of(TMP_DIR, fileConfigurationProperties.getBaseDir());
    }

    /**
     * 初期化処理
     */
    @PostConstruct
    public void init() {
        // 一時ファイル保存ディレクトリを作成する
        tempFilesDirPath.toFile().mkdirs();
    }

    @Override
    public File createTempFile(final String prefix, final String suffix) {
        try {
            return Files.createTempFile(tempFilesDirPath, prefix, suffix).toFile();
        } catch (IOException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_TMPFL_9001, tempFilesDirPath.toString(), prefix,
                    suffix);
        }
    }

    /**
     * 一時ファイルを定期的に削除する処理
     * 
     */
    @Scheduled(initialDelayString = FileConfigurationProperties.FILE_DELETE_TEMPFILES_INITIAL_DELAY_SECONDS_PROPERTY, //
            fixedRateString = FileConfigurationProperties.FILE_DELETE_TEMPFILES_FIXED_RATE_SECONDS_PROPERTY, timeUnit = TimeUnit.SECONDS)
    // @Scheduledで実行されるため、アクセス修飾子をprivateにできないのでprotected
    protected void deleteTempFiles() {
        appLogger.debug("Delete temporary files Task run.");
        // javaの一時ディレクトリにあるファイルを取得する
        File[] files = tempFilesDirPath.toFile().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                // 一時ファイルの更新日時を取得し、現在時刻より指定時間経過していた場合は、ファイル削除する
                FileTime lastModifiedTime = Files.getLastModifiedTime(file.toPath());
                if (lastModifiedTime.toMillis() < System.currentTimeMillis()
                        - fileConfigurationProperties.getExpirationSeconds() * 1000) {
                    Files.delete(file.toPath());
                    appLogger.debug("Delete temporary file: {}", file.getAbsolutePath());
                }
            } catch (IOException e) {
                appLogger.warn(CommonFrameworkMessageIds.W_FW_RPRT_8002, e, file.getName());
            }
        }
    }

}
