package com.example.fw.web.token;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoredTransactionTokenRepository {

    StoredTransactionToken findOneForUpdate(@Param("tokenName") String tokenName, @Param("tokenKey") String tokenKey, @Param("sessionId") String sessionId);


    int delete(@Param("tokenName") String tokenName, @Param("tokenKey") String tokenKey, @Param("sessionId") String sessionId);


    int insert(StoredTransactionToken token);


    int deleteOlderThanLatest(@Param("tokenName") String tokenName, @Param("sessionId") String sessionId, @Param("num") int num);


    int deleteBySessionId(@Param("sessionId") String sessionId);
}
