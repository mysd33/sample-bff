package com.example.bff.domain.service.async;

import com.example.fw.common.async.model.JobRequest;

/**
 * 
 * 非同期実行処理依頼のServiceインタフェース
 * 
 *
 */
public interface AsyncService {
    /**
     * 非同期実行を依頼する
     * 
     * @param jobRequest 非同期実行依頼
     */
    void invokeAsync(JobRequest jobRequest);
}
