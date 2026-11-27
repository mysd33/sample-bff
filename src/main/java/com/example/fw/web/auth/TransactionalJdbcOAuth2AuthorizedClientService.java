package com.example.fw.web.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.transaction.annotation.Transactional;


/// JDBCを利用したOAuth2AuthorizedClientServiceのトランザクション対応実装
/// 標準実装のJdbcOAuth2AuthorizedClientServiceは、auto-commitがtrueの状態で動作する
/// spring.datasource.hikari.auto-commit=falseの設定を有効にしている場合、レコードがインサートされない不具合が発生する。
/// これを回避するため、JdbcOAuth2AuthorizedClientServiceの操作めでトランザクションを有効にするため、@Transactionalアノテーションを付与したラッパークラスを作成する。

@RequiredArgsConstructor
@Transactional
public class TransactionalJdbcOAuth2AuthorizedClientService implements
    OAuth2AuthorizedClientService {

    private final JdbcOAuth2AuthorizedClientService delegate;

    @Transactional(readOnly = true)
    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> @Nullable T loadAuthorizedClient(
        @NonNull String clientRegistrationId,
        @NonNull String principalName) {
        return (T) delegate.loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Override
    public void saveAuthorizedClient(@NonNull OAuth2AuthorizedClient authorizedClient,
        @NonNull Authentication principal) {
        delegate.saveAuthorizedClient(authorizedClient, principal);
    }

    @Override
    public void removeAuthorizedClient(@NonNull String clientRegistrationId,
        @NonNull String principalName) {
        delegate.removeAuthorizedClient(clientRegistrationId, principalName);
    }
}
