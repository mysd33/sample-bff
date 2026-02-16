package com.example.fw.web.converter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.fw.common.utils.JapaneseStringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 特殊文字のコードポイント変換を行うリクエストラッパークラス
 */
public class SpecialCharConvertRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> convertedParams;

    public SpecialCharConvertRequestWrapper(HttpServletRequest request) {
        super(request);

        convertedParams = request.getParameterMap().entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Arrays.stream(e.getValue())//
                        .map(JapaneseStringUtils::exchageSpecialChar).toArray(String[]::new)));
    }

    @Override
    public String getParameter(String name) {
        String[] values = convertedParams.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return convertedParams.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return convertedParams;
    }
}
