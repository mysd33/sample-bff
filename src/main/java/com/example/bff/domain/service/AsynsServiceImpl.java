package com.example.bff.domain.service;

import org.springframework.stereotype.Service;

import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.async.repository.JobRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsynsServiceImpl implements AsyncService {
	private final JobRequestRepository jobRequestRepository;

	@Override
	public void invokeAsync(JobRequest jobRequest) {
		jobRequestRepository.save(jobRequest);
	}

}
