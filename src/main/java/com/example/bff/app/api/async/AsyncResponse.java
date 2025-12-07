package com.example.bff.app.api.async;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 
 * ジョブ処理依頼の受理結果を返却するクラス
 *
 */
public class AsyncResponse implements Serializable {

    @Schema(description = "受理結果")
    @Getter
    private final String result;

    // 受理
    public static final AsyncResponse ACCEPT = new AsyncResponse("accept");
    // 拒絶
    public static final AsyncResponse Reject = new AsyncResponse("reject");

    @Serial
    private static final long serialVersionUID = 8603261817673346760L;

    private AsyncResponse(final String result) {
        this.result = result;
    }

}
