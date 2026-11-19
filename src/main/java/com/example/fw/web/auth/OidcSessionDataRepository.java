package com.example.fw.web.auth;

import java.util.Set;

/// OIDCセッションデータの保存・取得を行うリポジトリ
public interface OidcSessionDataRepository {

    /// セッションデータを保存する
    void save(OidcSessionData data);

    /// クライアントセッションIDに紐づくセッションデータを取得する
    OidcSessionData findByClientSessionId(String clientSessionId);

    /// クライアントセッションIDに紐づくセッションデータを削除する
    OidcSessionData removeByClientSessionId(String clientSessionId);

    /// issuerとsidに紐づくクライアントセッションIDを取得する
    Set<String> findClientSessionIdsBySid(String issuer, String sid);

    /// issuerとsubに紐づくクライアントセッションIDを取得する
    Set<String> findClientSessionIdsBySubject(String issuer, String sub);
}
