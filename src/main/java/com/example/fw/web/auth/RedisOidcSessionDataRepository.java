package com.example.fw.web.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/// Redisを使用したSessionDataRepositoryの実装クラス
@RequiredArgsConstructor
public class RedisOidcSessionDataRepository implements OidcSessionDataRepository {

    private static final String SESSION_KEY_PREFIX = "oidc:session:";
    private static final String SID_INDEX_PREFIX = "oidc:idx:sid:";
    private static final String SUB_INDEX_PREFIX = "oidc:idx:sub:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration defaultTtl;

    @Override
    public void save(OidcSessionData data) {
        try {
            var clientSessionId = data.getClientSessionId();
            var ttl = Duration.between(Instant.now(), data.getExpiresAt());
            if (ttl.isNegative() || ttl.isZero()) {
                ttl = defaultTtl;
            }
            // OidcSessionDataをJSON文字列にシリアライズしてRedisに保存する
            var json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + clientSessionId, json, ttl);
            // sidとsubからクライアントセッションIDの取得するインデックスも作成してRedisに保存する
            if (StringUtils.hasText(data.getSid())) {
                saveIndex(SID_INDEX_PREFIX + data.getIssuer() + ":" + data.getSid(),
                    clientSessionId,
                    ttl);
            }
            if (StringUtils.hasText(data.getSub())) {
                saveIndex(SUB_INDEX_PREFIX + data.getIssuer() + ":" + data.getSub(),
                    clientSessionId,
                    ttl);
            }
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize SessionData", e);
        }
    }

    @Override
    public OidcSessionData findByClientSessionId(String clientSessionId) {
        // RedisからクライアントセッションIDに対応するSessionDataを取得する
        String json = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + clientSessionId);
        if (json == null) {
            return null;
        }
        return deserialize(json);
    }

    @Override
    public OidcSessionData removeByClientSessionId(String clientSessionId) {
        // RedisからクライアントセッションIDに対応するSessionDataを削除する
        String json = redisTemplate.opsForValue()
            .getAndDelete(SESSION_KEY_PREFIX + clientSessionId);
        if (json == null) {
            return null;
        }
        // 削除したSessionDataのsidとsubのインデックスも削除する
        var oidcSessionData = deserialize(json);
        if (StringUtils.hasText(oidcSessionData.getSid())) {
            removeFromIndex(
                SID_INDEX_PREFIX + oidcSessionData.getIssuer() + ":" + oidcSessionData.getSid(),
                clientSessionId);
        }
        if (StringUtils.hasText(oidcSessionData.getSub())) {
            removeFromIndex(
                SUB_INDEX_PREFIX + oidcSessionData.getIssuer() + ":" + oidcSessionData.getSub(),
                clientSessionId);
        }
        return oidcSessionData;
    }

    @Override
    public Set<String> findClientSessionIdsBySid(String issuer, String sid) {
        // sidをキーとするRedis上のインデックスからクライアントセッションIDのセットを取得する
        return findClientSessionIds(SID_INDEX_PREFIX + issuer + ":" + sid);
    }

    @Override
    public Set<String> findClientSessionIdsBySubject(String issuer, String sub) {
        // subをキーとするRedis上のインデックスからクライアントセッションIDのセットを取得する
        return findClientSessionIds(SUB_INDEX_PREFIX + issuer + ":" + sub);
    }

    /// Redis上の指定のキーに基づくインデックスにクライアントセッションIDを追加する
    ///
    /// @param key             Redis上のインデックスキー
    /// @param clientSessionId クライアントセッションID
    /// @param ttl             インデックスの有効期限
    private void saveIndex(String key, String clientSessionId, Duration ttl) {
        redisTemplate.opsForSet().add(key, clientSessionId);
        redisTemplate.expire(key, ttl);
    }

    /// Redis上のインデックスからクライアントセッションIDを削除する
    ///
    /// @param key             Redis上のインデックスキー
    /// @param clientSessionId クライアントセッションID
    private void removeFromIndex(String key, String clientSessionId) {
        redisTemplate.opsForSet().remove(key, clientSessionId);
    }

    /// Redis上の指定のキーに基づくインデックスからクライアントセッションIDのセットを取得する
    ///
    /// @param key Redis上のインデックスキー
    private Set<String> findClientSessionIds(String key) {
        Set<String> ids = redisTemplate.opsForSet().members(key);
        return ids != null ? ids : Collections.emptySet();
    }

    /// Redis上のSessionDataをJSON文字列からデシリアライズする
    ///
    /// @param json JSON文字列
    private OidcSessionData deserialize(String json) {
        try {
            return objectMapper.readValue(json, OidcSessionData.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize SessionData", e);
        }
    }
}
