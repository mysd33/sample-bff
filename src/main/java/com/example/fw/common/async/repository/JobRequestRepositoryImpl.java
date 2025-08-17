package com.example.fw.common.async.repository;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.logging.ApplicationLogger;
import com.example.fw.common.logging.LoggerFactory;
import com.example.fw.common.message.CommonFrameworkMessageIds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobRequestRepositoryの実装クラス キューにSQSを使用し、JobRequestを登録する
 *
 */
@XRayEnabled
@RequiredArgsConstructor
@Slf4j
public class JobRequestRepositoryImpl implements JobRequestRepository {
    private static final ApplicationLogger appLogger = LoggerFactory.getApplicationLogger(log);
    private final JmsTemplate jmsTemplate;
    private final String queueName;

    @Override
    public void save(final JobRequest jobRequest) {
        Assert.notNull(jobRequest, "jobRequestがNullです");
        // キューに登録
        jmsTemplate.convertAndSend(queueName, jobRequest);
        appLogger.info(CommonFrameworkMessageIds.I_FW_ASYNCCL_0001, queueName, jobRequest);
    }

}
