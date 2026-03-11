package com.prototype.api_test_suite.model;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record HarLog(
        String version,
        JsonNode creator,
        List<HarEntry> entries) {

}
