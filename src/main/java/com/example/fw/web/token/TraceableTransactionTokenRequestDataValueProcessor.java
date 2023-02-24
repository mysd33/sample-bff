package com.example.fw.web.token;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;

import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceableTransactionTokenRequestDataValueProcessor extends TransactionTokenRequestDataValueProcessor {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);

    @Override
    public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
        Map<String, String> fields = super.getExtraHiddenFields(request);
        appLogger.debug("hiddenFields[{}]: {}", request.getRequestURI(),  fields);
        return fields;
    }

}
