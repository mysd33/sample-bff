package com.example.bff.app;

import java.io.Serializable;

import lombok.Getter;

/**
 * 
 * ジョブ処理依頼の受理結果を返却するクラス
 *
 */
public class AsyncResponse implements Serializable {

	@Getter
	private final String result;

	// 受理
	public static final AsyncResponse ACCEPT = new AsyncResponse("accept");
	// 拒絶
	public static final AsyncResponse Reject = new AsyncResponse("reject");

	private static final long serialVersionUID = 8603261817673346760L;

	private AsyncResponse(final String result) {
		this.result = result;
	}

}
