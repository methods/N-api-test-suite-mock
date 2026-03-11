package com.prototype.api_test_suite.model;

public record HarEntry (
        String startedDateTime,
        HarRequest request,
        HarResponse response
){ }
