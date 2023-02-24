package com.example.fw.web.token;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import com.amazonaws.xray.AWSXRay;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.web.message.WebFrameworkMessageIds;

import lombok.extern.slf4j.Slf4j;

/**
 * セッションタイムアウト等のセッション破棄時にトークンを自動削除するHttpSessionListener
 *
 */
@Slf4j
public class TransactionTokenCleaningListener implements HttpSessionListener {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    @Autowired
    private StoredTransactionTokenRepository tokenRepository;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {        
        AWSXRay.beginSegment("transaction-token-clean");
        String sessionId = se.getSession().getId();

        //TODO:削除
        log.info("sessionDestroyed:" + sessionId);

        try {                        
            //対象のセッションのトランザクショントークンのレコードを削除
            int count = tokenRepository.deleteBySessionId(sessionId);
            if (count > 0) {
                appLogger.info(WebFrameworkMessageIds.I_ON_FW_0003, sessionId);
            }
        } catch (DataAccessException e) {
            appLogger.warn(WebFrameworkMessageIds.W_ON_FW_0001, e, sessionId);
        }
    }

}
