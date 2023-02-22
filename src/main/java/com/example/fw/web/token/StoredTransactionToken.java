package com.example.fw.web.token;

import lombok.Data;

@Data
public class StoredTransactionToken {
    private String tokenName;
    private String tokenKey;
    private String tokenValue;
    private String sessionId;
    private long sequence;
    
}
