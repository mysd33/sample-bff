package com.example.fw.common.async.repository;

import com.example.fw.common.async.model.JobRequest;

/**
 * 
 * 後続のバッチ処理を依頼するための非同期実行制御クラス
 *
 */
public interface JobRequestRepository {
    /**
     * バッチ処理を依頼する
     * 
     * @param jobRequest ジョブに渡す情報
     */
    void save(JobRequest jobRequest);

}
