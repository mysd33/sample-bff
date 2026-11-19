package com.example.fw.web.auth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.authentication.logout.LogoutTokenClaimNames;
import org.springframework.security.oauth2.client.oidc.authentication.logout.OidcLogoutToken;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionInformation;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionRegistry;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.util.StringUtils;

/// OidcSessionRegistryの実装クラス
/// OidcSessionRegistryのデフォルト実装がInMemoryOidcSessionRegistryであるため、
/// 複数台でのサーバ構成だとセッション情報が共有されず、バックチャネルログアウトが正しく動作しないため、
/// OidcSessionDataRepositoryに処理を委譲し外部のストアへ永続化してノード間でセッション情報を共有する実装を提供する。
@RequiredArgsConstructor
public class ExternalStoreOidcSessionRegistry implements OidcSessionRegistry {

    /// 定数"sid"がログアウトトークンの定数にしかないので、再定義
    private static final String SID = LogoutTokenClaimNames.SID;
    /// OIDCセッション情報を外部ストアの永続化に使用するSessionDataRepository
    private final OidcSessionDataRepository oidcSessionDataRepository;

    @Override
    public void saveSessionInformation(OidcSessionInformation info) {
        // OidcSessionInformationからRedisに保存するためのOidcSessionDataを作成する
        var clientSessionId = info.getSessionId();
        var oidcUser = info.getPrincipal();
        var issuer = oidcUser.getIssuer() != null ? oidcUser.getIssuer().toString() : "";
        List<String> audience =
            oidcUser.getAudience() != null ? oidcUser.getAudience() : List.of();
        var sid = oidcUser.getClaimAsString(SID);
        var sub = oidcUser.getSubject();
        var issuedAt =
            oidcUser.getIssuedAt() != null ? oidcUser.getIssuedAt() : Instant.now();
        var expiresAt = oidcUser.getExpiresAt();
        var oidcSessionData = OidcSessionData.builder()
            .clientSessionId(clientSessionId)
            .issuer(issuer)
            .audience(audience)
            .sid(sid)
            .sub(sub)
            .idTokenValue(oidcUser.getIdToken().getTokenValue())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .authorities(info.getAuthorities())
            .build();
        // OIDCセッション情報を外部ストアに保存する
        oidcSessionDataRepository.save(oidcSessionData);
    }

    @Override
    public OidcSessionInformation removeSessionInformation(@NonNull String clientSessionId) {
        // クライアントセッションIDが一致するOIDCセッション情報を外部ストアから削除する
        var oidcSessionData = oidcSessionDataRepository.removeByClientSessionId(clientSessionId);
        if (oidcSessionData == null) {
            return null;
        }
        // 削除したOIDCセッション情報をOidcSessionInformationを復元して返却する
        return toOidcSessionInformation(oidcSessionData);
    }

    @Override
    public @NonNull Iterable<OidcSessionInformation> removeSessionInformation(
        OidcLogoutToken logoutToken) {
        var issuer = logoutToken.getIssuer().toString();
        var sid = logoutToken.getSessionId();
        var sub = logoutToken.getSubject();
        var clientSessionIds = new HashSet<String>();
        // ログアウトトークンのsidが存在する場合はsidに紐づくクライアントセッションIDを取得し、
        // 存在しない場合はsubに紐づくクライアントセッションIDを取得する
        if (StringUtils.hasText(sid)) {
            clientSessionIds.addAll(
                oidcSessionDataRepository.findClientSessionIdsBySid(issuer, sid));
        } else if (StringUtils.hasText(sub)) {
            clientSessionIds.addAll(
                oidcSessionDataRepository.findClientSessionIdsBySubject(issuer, sub));
        }
        // 取得したクライアントセッションIDのリストから、ログアウトトークンに一致するセッション情報を削除する
        return removeOidcSessionInformation(clientSessionIds, logoutToken);
    }

    /// 対象のクライアントセッションIDのリストから、ログアウトトークンに一致するセッション情報を削除する
    ///
    /// @param clientSessionIds 削除対象のクライアントセッションID
    /// @param logoutToken      ログアウトトークン
    private @NonNull List<OidcSessionInformation> removeOidcSessionInformation(
        Set<String> clientSessionIds, OidcLogoutToken logoutToken) {
        var removed = new ArrayList<OidcSessionInformation>();
        for (String clientSessionId : clientSessionIds) {
            // クライアントセッションIDに紐づくOIDCセッションデータを取得する
            var oidcSessionData = oidcSessionDataRepository.findByClientSessionId(clientSessionId);
            if (oidcSessionData == null) {
                continue;
            }
            // ログアウトトークンとOidcセッション情報のissuer、audience、sidまたはsubが一致する場合は削除する
            if (matchesLogoutToken(logoutToken, oidcSessionData)) {
                var oidcSessionInformation = removeSessionInformation(clientSessionId);
                if (oidcSessionInformation != null) {
                    removed.add(oidcSessionInformation);
                }
            }
        }
        return removed;
    }

    /// ログアウトトークンとセッションデータのissuer、audience、sidまたはsubが一致するかを判定する
    private boolean matchesLogoutToken(OidcLogoutToken logoutToken, OidcSessionData data) {
        List<String> audience = data.getAudience() != null ? data.getAudience() : List.of();
        boolean sameProviderSession = StringUtils.hasText(logoutToken.getSessionId())
            ? logoutToken.getSessionId().equals(data.getSid())
            : StringUtils.hasText(logoutToken.getSubject())
                && logoutToken.getSubject().equals(data.getSub());

        return logoutToken.getIssuer().toString().equals(data.getIssuer())
            && !Collections.disjoint(logoutToken.getAudience(), audience)
            && sameProviderSession;
    }

    /// Redis上のSessionDataからOidcSessionInformationを復元する。
    private OidcSessionInformation toOidcSessionInformation(OidcSessionData data) {
        var claims = new HashMap<String, Object>();
        claims.put(IdTokenClaimNames.ISS, data.getIssuer());
        claims.put(IdTokenClaimNames.SUB, data.getSub());
        if (data.getAudience() != null && !data.getAudience().isEmpty()) {
            claims.put(IdTokenClaimNames.AUD, data.getAudience());
        }
        if (StringUtils.hasText(data.getSid())) {
            claims.put(SID, data.getSid());
        }

        var idToken = new OidcIdToken(
            data.getIdTokenValue(),
            data.getIssuedAt(),
            data.getExpiresAt(),
            claims
        );
        // ここでは、ダミーのOidcUserを作成作成している。OidcBackChannelLogoutHandlerでの使用しか想定しておらず
        // OidcUserの権限情報は使用されないことを前提にNO_AUTHORITIESとしてダミーのOidcUserを作成していることに注意する。
        var oidcUser = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES, idToken);
        return new OidcSessionInformation(data.getClientSessionId(), data.getAuthorities(),
            oidcUser);
    }
}
