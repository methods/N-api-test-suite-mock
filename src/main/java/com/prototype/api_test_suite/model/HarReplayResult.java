package com.prototype.api_test_suite.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HarReplayResult {
    private HarRequest originalRequest;
    private int httpStatus;
    private String responseBody;
    private String correlationId;
    private Map<String, Object> replayResponseData;
}
