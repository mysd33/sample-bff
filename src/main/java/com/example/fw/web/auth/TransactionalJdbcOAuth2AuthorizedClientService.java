package com.example.fw.web.auth;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.transaction.annotation.Transactional;


/// JDBCを利用したOAuth2AuthorizedClientServiceのトランザクション対応実装
/// 標準実装のJdbcOAuth2AuthorizedClientServiceは、auto-commitがtrueの状態で動作する
/// spring.datasource.hikari.auto-commit=falseの設定を有効にしている場合、レコードがインサートされない不具合が発生する。
/// これを回避するため、JdbcOAuth2AuthorizedClientServiceの操作めでトランザクションを有効にするため、@Transactionalアノテーションを付与したラッパークラスを作成する。
@Transactional
public class TransactionalJdbcOAuth2AuthorizedClientService extends
    JdbcOAuth2AuthorizedClientService {

    public TransactionalJdbcOAuth2AuthorizedClientService(
        org.springframework.jdbc.core.JdbcOperations jdbcOperations,
        org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository) {
        super(jdbcOperations, clientRegistrationRepository);
    }

    public TransactionalJdbcOAuth2AuthorizedClientService(JdbcOperations jdbcOperations,
        ClientRegistrationRepository clientRegistrationRepository, LobHandler lobHandler) {
        super(jdbcOperations, clientRegistrationRepository, lobHandler);
    }

    @Transactional(readOnly = true)
    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> @Nullable T loadAuthorizedClient(
        String clientRegistrationId,
        String principalName) {
        return (T) super.loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
        Authentication principal) {
        super.saveAuthorizedClient(authorizedClient, principal);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        super.removeAuthorizedClient(clientRegistrationId, principalName);
    }
}
