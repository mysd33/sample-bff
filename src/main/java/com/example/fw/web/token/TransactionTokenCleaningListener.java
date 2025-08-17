package com.example.fw.web.token;

import org.springframework.dao.DataAccessException;

import com.amazonaws.xray.AWSXRay;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.web.message.WebFrameworkMessageIds;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * セッションタイムアウト等のセッション破棄時にトークンを自動削除するHttpSessionListener
 * 
 * Redisを使用する場合は、Keyspace-Notificationを有効化して、キーの有効期限切れ（セッションタイムアウト）の検知ができるようにする。<br> 
 * 
 * Spring Session Data RedisはデフォルトではKeyspace-Notificationを有効化してくれるが、
 * ElastiCache for Redisではconfigコマンドの実行が禁止されているため、
 * 当該サンプルAPでは、application-production.ymlに「spring.session.redis.configure-action」を「none」で設定している<br>
 *  
 * redis-cliでconfigコマンドで設定する場合は、以下実行<br>
 * config set notify-keyspace-events gxE<br>
 * <br>
 * ElastiCache for Redisの場合は、Keyspace-Notificationを有効化するには、カスタムキャッシュパラメータグループに
 * 「notify-keyspace-events」パラメータを「gxE」に設定する<br>
 * 
 * また、HttpSessionListenerによる、セッションの有効期限切れ等のイベントを検知するには、
 * Spring Session Data RedisのデフォルトのSessionRepository実装であるRedisSessionRepositoryクラスはセッションの有効期限切れと削除もサポートしていないため
 * 実装を、RedisIndexedSessionRepositoryクラスに切り替える必要がある。
 * 当該サンプルAPでは、application-production.ymlに「spring.session.redis.repository-type」を「indexed」で設定している<br> 
 * 
 * @see <a href="https://docs.spring.io/spring-session/reference/configuration/redis.html#choosing-between-regular-and-indexed">Choosing Between RedisSessionRepository and RedisIndexedSessionRepository</> 
 * @see <a href="https://docs.spring.io/spring-session/reference/api.html#api-redisindexedsessionrepository-sessiondestroyedevent">Spring Session Document</a>
 * @see <a href="https://aws.amazon.com/jp/premiumsupport/knowledge-center/elasticache-redis-keyspace-notifications/">AWS - Amazon ElastiCache で Redis キースペース通知を実装するにはどうすればよいですか?</a>
 * 
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionTokenCleaningListener implements HttpSessionListener {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);    
    private final StoredTransactionTokenRepository tokenRepository;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {        
        AWSXRay.beginSegment("transaction-token-clean");
        String sessionId = se.getSession().getId();

        appLogger.debug("sessionDestroyed:" + sessionId);

        try {                        
            //対象のセッションのトランザクショントークンのレコードを削除
            int count = tokenRepository.deleteBySessionId(sessionId);
            if (count > 0) {
                appLogger.info(WebFrameworkMessageIds.I_FW_TRNTKN_0001, sessionId);
            }
        } catch (DataAccessException e) {
            appLogger.warn(WebFrameworkMessageIds.W_FW_TRNTKN_8001, e, sessionId);
        }
    }

}
