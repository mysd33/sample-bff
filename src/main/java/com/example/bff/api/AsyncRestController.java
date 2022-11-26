package com.example.bff.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bff.domain.service.AsyncService;
import com.example.fw.common.async.model.JobRequest;

import lombok.RequiredArgsConstructor;

/**
 * 
 * ジョブ登録を依頼する汎用的な Web API
 * 汎用的なAPIにするとジョブパラメータに意味を持たせれられないので
 * その場合は、通常のWebAPIを使用する
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/async")
public class AsyncRestController {	
	private final AsyncService asyncService;
	
	@GetMapping("/{jobId:.+}")
    @ResponseStatus(HttpStatus.OK)	
	public AsyncResponse executeBatch(			
			@PathVariable("jobId") final String jobId,
			@RequestParam("param01") final String param01,
			@RequestParam("param02") final String param02) {
		Map<String, String > parameters = new HashMap<>();
		parameters.put("param01", param01);
		parameters.put("param02", param02);
		asyncService.invokeAsync(
				JobRequest.builder()
				.jobId(jobId)
				.parameters(parameters)
				.build());
		return AsyncResponse.ACCEPT;
	}
	
	@PostMapping
    @ResponseStatus(HttpStatus.OK)
	public AsyncResponse executeBatch(@RequestBody final JobRequest jobRequest) {			
		asyncService.invokeAsync(jobRequest);				
		return AsyncResponse.ACCEPT;		
	}
}
