package com.example.fw.web.token;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.terasoluna.gfw.web.token.TokenStringGenerator;
import org.terasoluna.gfw.web.token.transaction.TransactionToken;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenStore;

/**
 * DB管理(MyBatis)によるTransactionTokenStore実装クラス 
 *
 */
public class MyBatisTransactionTokenStore implements TransactionTokenStore {
    private final int transactionTokenSizePerTokenName;
    private final TokenStringGenerator generator;

    @Autowired
    private StoredTransactionTokenRepository tokenRepository;    

    public MyBatisTransactionTokenStore() {
        this(10, new TokenStringGenerator());
    }
    
    public MyBatisTransactionTokenStore(int transactionTokenSizePerTokenName) {
        this(transactionTokenSizePerTokenName, new TokenStringGenerator());
    }

    public MyBatisTransactionTokenStore(int transactionTokenSizePerTokenName, TokenStringGenerator generator) {
        this.transactionTokenSizePerTokenName = transactionTokenSizePerTokenName;
        this.generator = generator;
    }    

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getAndClear(TransactionToken transactionToken) {
        String name = transactionToken.getTokenName();
        String key = transactionToken.getTokenKey();
        String sessionId = getSession().getId();

        try {
            StoredTransactionToken token = tokenRepository.findOneForUpdate(name, key, sessionId);
            if (token == null) {
                return null;
            }

            tokenRepository.delete(name, key, sessionId);
            return token.getTokenValue();
        } catch (PessimisticLockingFailureException e) {
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(TransactionToken transactionToken) {
        String name = transactionToken.getTokenName();
        String key = transactionToken.getTokenKey();
        String sessionId = getSession().getId();
        tokenRepository.delete(name, key, sessionId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String createAndReserveTokenKey(String tokenName) {
        String sessionId = getSession().getId();
        tokenRepository.deleteOlderThanLatest(tokenName, sessionId, transactionTokenSizePerTokenName - 1);
        return generator.generate(UUID.randomUUID().toString());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(TransactionToken transactionToken) {
        StoredTransactionToken token = new StoredTransactionToken();
        token.setTokenName(transactionToken.getTokenName());
        token.setTokenKey(transactionToken.getTokenKey());
        token.setTokenValue(transactionToken.getTokenValue());
        token.setSessionId(getSession().getId());
        tokenRepository.insert(token);

        getSession();
    }

    HttpSession getSession() {
        return getRequest().getSession(true);
    }

    HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}