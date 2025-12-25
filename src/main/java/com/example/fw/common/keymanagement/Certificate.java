package com.example.fw.common.keymanagement;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import lombok.Builder;
import lombok.Value;

/**
 * 証明書を表すためのクラス
 */
@Value
@Builder
public class Certificate {
    private static final String PEM_TYPE = "CERTIFICATE";
    private final byte[] der; // DER形式でエンコードされたCSRデータ

    /**
     * X509Certificateオブジェクトを取得します。
     *
     * @return X509Certificateオブジェクト
     * @throws CertificateException 証明書の生成に失敗した場合
     * @throws IOException          入出力エラーが発生した場合
     */
    public X509Certificate getX509Certificate() throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(der)) {
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        }
    }

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
