package com.prototype.api_test_suite.model;

import java.util.List;

public record HarRequest (
        String method,
        String url,
        List<HarHeader> headers,
        HarPostData postData
){}
