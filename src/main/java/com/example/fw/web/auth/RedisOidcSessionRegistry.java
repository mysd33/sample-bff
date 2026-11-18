package com.example.fw.web.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.authentication.logout.OidcLogoutToken;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionInformation;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionRegistry;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


/// Redisを使用したOidcSessionRegistryの実装クラス
public class RedisOidcSessionRegistry implements OidcSessionRegistry {

    private static final String SESSION_KEY_PREFIX = "oidc:session:";
    private static final String SID_INDEX_PREFIX = "oidc:idx:sid:";
    private static final String SUB_INDEX_PREFIX = "oidc:idx:sub:";
    private static final String SID = "sid";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration defaultTtl;

    /// コンストラクタ
    ///
    /// @param redisTemplate RedisTemplate
    /// @param objectMapper  ObjectMapper
    /// @param defaultTtl    Redisに保存する際にトークンの有効期限がなかった場合のデフォルトTTL
    public RedisOidcSessionRegistry(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
        Duration defaultTtl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public void saveSessionInformation(OidcSessionInformation info) {
        String clientSessionId = info.getSessionId();
        OidcUser principal = info.getPrincipal();
        String issuer = principal.getIssuer() != null ? principal.getIssuer().toString() : "";
        List<String> audience =
            principal.getAudience() != null ? principal.getAudience() : List.of();
        String sid = principal.getClaimAsString(SID);
        String sub = principal.getSubject();
        Instant issuedAt =
            principal.getIssuedAt() != null ? principal.getIssuedAt() : Instant.now();
        Instant expiresAt = principal.getExpiresAt() != null ? principal.getExpiresAt()
            : Instant.now().plus(defaultTtl);

        // OidcSessionInformationをもとにRedisに保存するためのSessionDataを作成する
        SessionData data = new SessionData(
            clientSessionId,
            issuer,
            audience,
            sid,
            sub,
            principal.getIdToken().getTokenValue(),
            issuedAt,
            expiresAt,
            info.getAuthorities()
        );
        // Redisに保存する際のTTLを計算する
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = defaultTtl;
        }

        try {
            // SessionDataをJSONにシリアライズして、クライアントセッションIDをキーとしてRedisに保存する
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + clientSessionId, json, ttl);
            // sidとsubからクライアントセッションIDを取得できるインデックスを作成する
            if (StringUtils.hasText(sid)) {
                String sidKey = SID_INDEX_PREFIX + issuer + ":" + sid;
                redisTemplate.opsForSet().add(sidKey, clientSessionId);
                redisTemplate.expire(sidKey, ttl);
            }
            if (StringUtils.hasText(sub)) {
                String subKey = SUB_INDEX_PREFIX + issuer + ":" + sub;
                redisTemplate.opsForSet().add(subKey, clientSessionId);
                redisTemplate.expire(subKey, ttl);
            }
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize OidcSessionInformation", e);
        }
    }

    @Override
    public OidcSessionInformation removeSessionInformation(@NonNull String clientSessionId) {
        // Redisからセッション情報を取得して削除する
        String json = redisTemplate.opsForValue()
            .getAndDelete(SESSION_KEY_PREFIX + clientSessionId);
        if (json == null) {
            return null;
        }
        try {
            // sidとsubからクライアントセッションIDを取得するためのインデックスも削除する
            SessionData data = objectMapper.readValue(json, SessionData.class);
            if (StringUtils.hasText(data.sid())) {
                redisTemplate.opsForSet()
                    .remove(SID_INDEX_PREFIX + data.issuer() + ":" + data.sid(), clientSessionId);
            }
            if (StringUtils.hasText(data.sub())) {
                redisTemplate.opsForSet()
                    .remove(SUB_INDEX_PREFIX + data.issuer() + ":" + data.sub(), clientSessionId);
            }
            return toOidcSessionInformation(data);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize OidcSessionInformation", e);
        }
    }

    @Override
    public @NonNull Iterable<OidcSessionInformation> removeSessionInformation(
        OidcLogoutToken logoutToken) {
        // ログアウトトークンからissuer、sid、subを取得して削除対象のセッションIDを特定する
        String issuer = logoutToken.getIssuer().toString();
        String sid = logoutToken.getSessionId();
        String sub = logoutToken.getSubject();

        Set<String> clientSessionIds = new HashSet<>();
        if (StringUtils.hasText(sid)) {
            // ログアウトトークンにsid（セッションID）があれば、インデックスを参照してセッションIDを取得して削除対象とする
            Set<String> ids = redisTemplate.opsForSet()
                .members(SID_INDEX_PREFIX + issuer + ":" + sid);
            if (ids != null) {
                clientSessionIds.addAll(ids);
            }
        } else if (StringUtils.hasText(sub)) {
            // sidがなければ、sub（ユーザID）からインデックスを参照して合致するセッションIDを取得して削除対象とする
            Set<String> ids = redisTemplate.opsForSet()
                .members(SUB_INDEX_PREFIX + issuer + ":" + sub);
            if (ids != null) {
                clientSessionIds.addAll(ids);
            }
        }
        return removeOidcSessionInformation(clientSessionIds, logoutToken);
    }

    /// 対象のクライアントセッションIDのリストから、ログアウトトークンに一致するセッション情報を削除する
    ///
    /// @param clientSessionIds 削除対象のクライアントセッションID
    /// @param logoutToken      ログアウトトークン
    private @NonNull List<OidcSessionInformation> removeOidcSessionInformation(
        Set<String> clientSessionIds, OidcLogoutToken logoutToken) {
        List<OidcSessionInformation> removed = new ArrayList<>();
        for (String clientSessionId : clientSessionIds) {
            // Redisから対象のクライアントセッションIDのセッション情報を取得する
            String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + clientSessionId);
            if (json == null) {
                continue;
            }
            try {
                SessionData data = objectMapper.readValue(json, SessionData.class);
                if (matchesLogoutToken(logoutToken, data)) {
                    // ログアウトトークンとセッション情報が一致する場合は、Redisから削除する
                    OidcSessionInformation info = removeSessionInformation(clientSessionId);
                    if (info != null) {
                        removed.add(info);
                    }
                }
            } catch (JacksonException e) {
                throw new IllegalStateException("Failed to deserialize OidcSessionInformation", e);
            }
        }
        return removed;
    }

    /// ログアウトトークンとセッションデータのissuer、audience、sidまたはsubが一致するかを判定する
    ///
    /// @param logoutToken ログアウトトークン
    /// @param data        セッションデータ
    /// @return 一致する場合はtrue、一致しない場合はfalse
    private boolean matchesLogoutToken(OidcLogoutToken logoutToken, SessionData data) {
        List<String> audience = data.audience() != null ? data.audience() : List.of();
        // 再度念のため、sidまたはsubが一致するかを判定する
        boolean sameProviderSession = StringUtils.hasText(logoutToken.getSessionId())
            ? logoutToken.getSessionId().equals(data.sid())
            : StringUtils.hasText(logoutToken.getSubject())
                && logoutToken.getSubject().equals(data.sub());

        // issuerが一致し、audienceのいずれかが一致し、sidまたはsubが一致する場合にtrueを返す
        return logoutToken.getIssuer().toString().equals(data.issuer())
            && !Collections.disjoint(logoutToken.getAudience(), audience)
            && sameProviderSession;
    }

    /// Redis上のSessionDataからOidcSessionInformationを復元する。
    ///
    /// @param data セッションデータ
    /// @return 復元されたOidcSessionInformation
    private OidcSessionInformation toOidcSessionInformation(SessionData data) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimNames.ISS, data.issuer());
        claims.put(JwtClaimNames.SUB, data.sub());
        if (data.audience() != null && !data.audience().isEmpty()) {
            claims.put(JwtClaimNames.AUD, data.audience());
        }
        if (StringUtils.hasText(data.sid())) {
            claims.put(SID, data.sid());
        }

        OidcIdToken idToken = new OidcIdToken(
            data.idTokenValue(),
            data.issuedAt(),
            data.expiresAt(),
            claims
        );
        // ダミーのOidcUserを作成する
        // NO_AUTHORITIESを指定しているため権限は空になり実際のOidcUserを復元できていないが
        // OidcBackendLogoutHandlerではOidcUserの権限は使用されないため問題ない
        OidcUser user = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES, idToken);
        return new OidcSessionInformation(data.clientSessionId(), data.authorities(), user);
    }

    /// Redisに保存するセッション情報のデータクラス
    public record SessionData(
        String clientSessionId,
        String issuer,
        List<String> audience,
        String sid,
        String sub,
        String idTokenValue,
        Instant issuedAt,
        Instant expiresAt,
        Map<String, String> authorities
    ) {

    }
}
