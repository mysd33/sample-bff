package com.example.bff.domain.service.async;

import org.springframework.stereotype.Service;

import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.async.repository.JobRequestRepository;

import lombok.RequiredArgsConstructor;

/**
 * 
 * 非同期実行処理依頼のService実装クラス
 * 
 *
 */
@Service
@RequiredArgsConstructor
public class AsynsServiceImpl implements AsyncService {
    private final JobRequestRepository jobRequestRepository;

    @Override
    public void invokeAsync(JobRequest jobRequest) {
        jobRequestRepository.save(jobRequest);
    }

}
