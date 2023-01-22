package com.example.fw.web.view;

import static com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractView;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import lombok.Setter;
import lombok.val;

/**
 * CSVダウンロード用のビュークラス
 */
public class CsvDownloadView extends AbstractView {

    protected static final CsvMapper csvMapper = createCsvMapper();

    protected Class<?> clazz;

    protected Collection<?> data;

    @Setter
    protected String filename;

    @Setter
    protected List<String> columns;

    /**
     * CSVマッパーを生成する。
     *
     * @return
     */
    static CsvMapper createCsvMapper() {
        CsvMapper mapper = new CsvMapper();
        mapper.configure(ALWAYS_QUOTE_STRINGS, true);
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * コンストラクタ
     *
     * @param clazz    CSVにマッピングするデータクラス
     * @param data     対象のデータ
     * @param fileName ファイル名
     */
    public CsvDownloadView(final Class<?> clazz, final Collection<?> data, final String fileName) {
        Assert.notNull(clazz, "clazzがNullです");
        Assert.notNull(data, "dataがNullです");
        Assert.notNull(fileName, "fileNameがNullです");
        setContentType("application/octet-stream; charset=Windows-31J;");
        this.clazz = clazz;
        this.data = data;
        this.filename = fileName;
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected final void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        // ファイル名に日本語を含めても文字化けしないようにUTF-8にエンコードする
        val encodedFilename = encodeUtf8(filename);
        val contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);

        response.setHeader(CONTENT_TYPE, getContentType());
        response.setHeader(CONTENT_DISPOSITION, contentDisposition);

        // CSVヘッダをオブジェクトから作成する
        CsvSchema schema = csvMapper.schemaFor(clazz).withHeader();

        if (columns != null && columns.isEmpty()) {
            // カラムが指定された場合は、スキーマを再構築する
            val builder = schema.rebuild().clearColumns();
            for (String column : columns) {
                builder.addColumn(column);
            }
            schema = builder.build();
        }

        // 書き出し
        val outputStream = new BufferedOutputStream(response.getOutputStream());
        try (Writer writer = new OutputStreamWriter(outputStream, "Windows-31J")) {
            csvMapper.writer(schema).writeValue(writer, data);
        }
    }

    private String encodeUtf8(final String filename) {
        String encoded = null;

        try {
            encoded = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // 例外は発生しない
        }

        return encoded;
    }
}