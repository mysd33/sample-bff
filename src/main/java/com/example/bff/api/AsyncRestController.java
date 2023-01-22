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

import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.service.AsyncService;
import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 
 * ジョブ登録を依頼する汎用的な Web API 汎用的なAPIにするとジョブパラメータに意味を持たせれられないので その場合は、通常のWebAPIを使用する
 */
@Tag(name = "非同期実行管理", description = "非同期実行管理API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/async")
public class AsyncRestController {
    private final AsyncService asyncService;

    @Operation(summary = "ジョブ実行依頼", description = "ジョブの実行を後方のバッチAPへ依頼します。")
    @GetMapping("/{jobId:.+}")
    @ResponseStatus(HttpStatus.OK)
    public AsyncResponse executeBatch(@Parameter(description = "ジョブID") @PathVariable("jobId") final String jobId,
            @Parameter(description = "ジョブパラメータ1") @RequestParam("param01") final String param01,
            @Parameter(description = "ジョブパラメータ2") @RequestParam("param02") final String param02) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("param01", param01);
        parameters.put("param02", param02);
        asyncService.invokeAsync(JobRequest.builder().jobId(jobId).parameters(parameters).build());
        return AsyncResponse.ACCEPT;
    }

    @Operation(summary = "ジョブ再実行依頼", description = "ジョブの再実行を後方のバッチAPへ依頼します。")
    @GetMapping("/restart/{jobExecutionId:.+}")
    public AsyncResponse restartBatch(
            @Parameter(description = "ジョブ再実行ID") @PathVariable("jobExecutionId") final Long jobExecutionId) {
        asyncService.invokeAsync(JobRequest.builder().restart(true).jobExecutionId(jobExecutionId).build());
        return AsyncResponse.ACCEPT;
    }

    @Operation(summary = "ジョブ実行依頼", description = "ジョブの実行を後方のバッチAPへ依頼します。")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public AsyncResponse executeBatch(@Parameter(description = "ジョブリクエスト") @RequestBody final JobRequest jobRequest) {
        if (!jobRequest.isValid()) {
            throw new BusinessException(MessageIds.W_EX_8003);
        }
        if (jobRequest.isRestart()) {
            throw new BusinessException(MessageIds.W_EX_8004);
        }
        asyncService.invokeAsync(jobRequest);
        return AsyncResponse.ACCEPT;
    }

    @Operation(summary = "ジョブ再実行依頼", description = "ジョブの実行を後方のバッチAPへ依頼します。")
    @PostMapping("/restart")
    @ResponseStatus(HttpStatus.OK)
    public AsyncResponse restartBatch(@Parameter(description = "ジョブリクエスト") @RequestBody final JobRequest jobRequest) {
        if (!jobRequest.isValid()) {
            throw new BusinessException(MessageIds.W_EX_8003);
        }
        if (!jobRequest.isRestart()) {
            throw new BusinessException(MessageIds.W_EX_8004);
        }
        asyncService.invokeAsync(jobRequest);
        return AsyncResponse.ACCEPT;
    }
}
