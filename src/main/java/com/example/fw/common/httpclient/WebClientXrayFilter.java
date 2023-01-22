package com.example.fw.common.httpclient;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;

/**
 * 
 * WebClient呼び出し時のX-Rayのトレースを引き継ぐためにHTTPヘッダー情報を引き渡す機能
 * 
 *
 */
public class WebClientXrayFilter {
    /**
     * WebClient呼び出し時のX-Rayのトレースを引き継ぐためにHTTPヘッダー情報を引き渡す
     */
    public ExchangeFilterFunction filter() {
        return (clientRequest, nextFilter) -> {
            Segment segment = AWSXRay.getCurrentSegment();
            Subsegment subsegment = AWSXRay.getCurrentSubsegment();
            TraceHeader traceHeader = new TraceHeader(segment.getTraceId(),
                    segment.isSampled() ? subsegment.getId() : null,
                    segment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
            ClientRequest newClientRequest = ClientRequest.from(clientRequest)
                    .header(TraceHeader.HEADER_KEY, traceHeader.toString()).build();
            return nextFilter.exchange(newClientRequest);
        };
    }
}
