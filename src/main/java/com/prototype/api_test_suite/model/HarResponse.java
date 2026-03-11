package com.prototype.api_test_suite.model;

import java.util.List;

public record HarResponse(
        int status,
        String statusText,
        List<HarHeader> headers,
        HarContent content
) {
}
