package com.example.bff.app.api.async;

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

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.service.async.AsyncService;
import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.exception.BusinessException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * ジョブ登録を依頼する汎用的な Web APIを提供するRestControllerクラス
 * 
 */
@XRayEnabled
@Tag(name = "非同期実行管理", description = "非同期実行管理API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/async")
public class AsyncRestController {
    private final AsyncService asyncService;

    /**
     * ブラウザのURLで、/api/v1/async/{jobId}?param01=xxx&param02=yyy 入力するだけで簡単にジョブ実行依頼が行えるようにGETメソッドを定義します。
     * @param jobId ジョブID（ジョブ名）
     * @param param01 ジョブパラメータ1
     * @param param02　ジョブパラメータ2
     * @return
     */
    @Operation(summary = "ジョブ実行依頼", description = "ジョブの実行を後方のバッチAPへ依頼します。")
    @GetMapping("/{jobId:.+}")
    @ResponseStatus(HttpStatus.OK)
    public AsyncResponse executeBatch(@Parameter(description = "ジョブID") @PathVariable final String jobId,
            @Parameter(description = "ジョブパラメータ1") @RequestParam final String param01,
            @Parameter(description = "ジョブパラメータ2") @RequestParam final String param02) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("param01", param01);
        parameters.put("param02", param02);
        asyncService.invokeAsync(JobRequest.builder().jobId(jobId).parameters(parameters).build());
        return AsyncResponse.ACCEPT;
    }

    /**
     * ブラウザのURLで、/api/v1/async/restart/{jobExecutionId} 入力するだけで簡単にジョブ再実行依頼が行えるようにGETメソッドを定義します。
     * @param jobExecutionId ジョブ実行ID
     * @return
     */
    @Operation(summary = "ジョブ再実行依頼", description = "ジョブの再実行を後方のバッチAPへ依頼します。")
    @GetMapping("/restart/{jobExecutionId:.+}")
    public AsyncResponse restartBatch(@Parameter(description = "ジョブ再実行ID") @PathVariable final Long jobExecutionId) {
        asyncService.invokeAsync(JobRequest.builder().restart(true).jobExecutionId(jobExecutionId).build());
        return AsyncResponse.ACCEPT;
    }

    /**
     * ジョブ実行依頼を行うためのPOSTメソッドを定義します。GETメソッドと違ってジョブパラメータが自由に設定できるようになります。
     * @param jobRequest ジョブリクエスト
     * @return
     */
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

    /**
     * ジョブ再実行依頼を行うためのPOSTメソッドを定義します。
     * @param jobRequest ジョブリクエスト
     * @return
     */
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
