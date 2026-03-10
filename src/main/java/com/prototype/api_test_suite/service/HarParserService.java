package com.prototype.api_test_suite.service;

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

    public List<Map<String, String>> extractPostRequests(String harFilePath) throws IOException {

        List<Map<String, String>> postRequests = new ArrayList<>();
        File harFile = new File(harFilePath);

        // Read the HAR file into a JsonNode tree
        JsonNode harRoot = objectMapper.readTree(harFile);

        // Navigate to the 'entries' array inside the 'log' key
        JsonNode entries = harRoot.path("log").path("entries");

        if (entries.isArray()) { // Each entry should be an array
            for (JsonNode entry : entries) {
                JsonNode request = entry.path("request");

                // Find the JSON entries with 'POST' in the 'method' key
                if ("POST".equals(request.path("method").asText())) {
                    // Extract the URL, headers(?), and body from the request
                    Map<String, String> requestDetails = new HashMap<>();
                    requestDetails.put("method", "POST");
                    requestDetails.put("url", request.path("url").asText());

                    // Extract the request body - 'text' field in 'postData' object
                    JsonNode postData = request.path("postData");
                    if (!postData.isMissingNode()) {
                        if (postData.path("text").isTextual()) {
                            requestDetails.put("body", postData.path("text").asText());
                        } else {
                            requestDetails.put("body", ""); // Body might not be text (e.g., multipart form)
                        }
                    } else {
                        requestDetails.put("body", "");
                    }

                    // Extract the headers
                    JsonNode headersNode = request.path("headers");
                    if (headersNode.isArray()) {
                        for (JsonNode header : headersNode) {
                            JsonNode nameNode = header.path("name");
                            JsonNode valueNode = header.path("value");

                            if (nameNode.isTextual() && valueNode.isTextual()) {
                                requestDetails.put("header-" + nameNode.asText().toLowerCase(), valueNode.asText());
                            }
                        }
                    }
                    postRequests.add(requestDetails);
                };
            }
        }
        return postRequests;
    }
}
