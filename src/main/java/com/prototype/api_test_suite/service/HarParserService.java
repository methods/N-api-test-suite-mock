package com.prototype.api_test_suite.service;

import com.prototype.api_test_suite.model.Har;
import com.prototype.api_test_suite.model.HarEntry;
import com.prototype.api_test_suite.model.HarRequest;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HarParserService {

    private final ObjectMapper objectMapper;

    public HarParserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<HarRequest> extractPostRequests(String harFilePath) throws IOException {

        List<HarRequest> postRequests = new ArrayList<>();
        File harFile = new File(harFilePath);
        // Map the HAR file to the Har Model
        Har har = objectMapper.readValue(harFile, Har.class);
        if (har != null && har.log() != null && har.log().entries() != null) {
            for (HarEntry entry : har.log().entries()) {
                if (entry.request() != null && "POST".equals(entry.request().method())) {
                    postRequests.add(entry.request());
                }
            }
        }
        return postRequests;
    };
}
