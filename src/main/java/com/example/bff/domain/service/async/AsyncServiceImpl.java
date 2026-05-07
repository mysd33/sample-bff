package com.example.bff.domain.service.async;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.async.repository.JobRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/// 非同期実行処理依頼のService実装クラス
@XRayEnabled
@Service
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {

    private final JobRequestRepository jobRequestRepository;

    @Override
    public void invokeAsync(JobRequest jobRequest) {
        jobRequestRepository.save(jobRequest);
    }

}
