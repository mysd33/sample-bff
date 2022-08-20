package com.example.bff.domain.service;

import com.example.fw.common.async.model.JobRequest;

public interface AsyncService {
	void invokeAsync(JobRequest jobRequest);
}
