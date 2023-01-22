package com.example.fw.common.async.repository;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

import com.example.fw.common.async.model.JobRequest;

import lombok.RequiredArgsConstructor;

/**
 * 
 * JobRequestRepositoryの実装クラス キューにSQSを使用し、JobRequestを登録する
 *
 */
@RequiredArgsConstructor
public class JobRequestRepositoryImpl implements JobRequestRepository {
    private final JmsTemplate jmsTemplate;
    private final String queueName;

    @Override
    public void save(final JobRequest jobRequest) {
        Assert.notNull(jobRequest, "jobRequestがNullです");
        // キューに登録
        jmsTemplate.convertAndSend(queueName, jobRequest);
    }

}
