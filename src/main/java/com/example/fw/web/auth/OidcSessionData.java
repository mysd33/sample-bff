package com.example.fw.web.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Redisに保存するOIDCセッション情報（OidcSessionInformation）のデータクラス
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OidcSessionData {

    /// クライアントセッションID
    private String clientSessionId;
    /// IDトークンのsid（OpenID ProviderとのセッションID）
    private String sid;
    /// IDトークンのSubject（sub）
    private String sub;
    /// IDトークンのIssuer(iss)
    private String issuer;
    /// IDトークンのaud（audience）
    private List<String> audience;
    /// IDトークンの値
    private String idTokenValue;
    /// IDトークンの発行日時
    private Instant issuedAt;
    /// IDトークンの有効期限
    private Instant expiresAt;
    /// OIDCセッション情報（OidcSessionInformation）に含まれている権限情報
    private Map<String, String> authorities;
}
