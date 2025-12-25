package com.example.fw.common.keymanagement;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import lombok.Builder;
import lombok.Value;

/**
 * 証明書署名要求（CSR:Certificate Signing Request）データを表すクラス
 */
@Value
@Builder
public class CertificateSigningRequest {
    private static final String PEM_TYPE = "CERTIFICATE REQUEST";
    private final byte[] der; // DER形式でエンコードされたCSRデータ

    /**
     * PEM形式でCSRデータをファイルにエクスポートします。
     *
     * @param filePath 保存先のファイルパス
     * @throws IOException 入出力エラーが発生した場合
     */
    public void exportPemTo(final String filePath) throws IOException {
        exportPemTo(new FileWriter(filePath));
    }

    /**
     * PEM形式でCSRデータを指定されたWriterにエクスポートします。
     *
     * @param writer 保存先のWriter
     * @throws IOException 入出力エラーが発生した場合
     */
    public void exportPemTo(final Writer writer) throws IOException {
        try (PemWriter pemWriter = new PemWriter(writer)) {
            pemWriter.writeObject(new PemObject(PEM_TYPE, der));
        }
    }
}
