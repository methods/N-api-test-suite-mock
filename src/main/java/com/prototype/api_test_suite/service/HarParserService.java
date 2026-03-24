package com.prototype.api_test_suite.service;

import com.prototype.api_test_suite.model.Har;
import com.prototype.api_test_suite.model.HarEntry;
import com.prototype.api_test_suite.model.HarHeader;
import com.prototype.api_test_suite.model.HarRequest;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HarParserService {

    private final ObjectMapper objectMapper;

    public HarParserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<HarEntry> extractPostRequests(String harFilePath) throws IOException {

        List<HarEntry> postEntries = new ArrayList<>();
        File harFile = new File(harFilePath);
        // Map the HAR file to the Har Model
        Har har = objectMapper.readValue(harFile, Har.class);

        if (har != null && har.log() != null && har.log().entries() != null) {
            System.out.println("Found " + har.log().entries().size() + " entries.");

            for (HarEntry entry : har.log().entries()) {
//                System.out.println("Processing entry: " + entry.request());

                if (entry.request() != null && "POST".equals(entry.request().method())) {
                    System.out.println("--- Found POST Request ---"); // Debug print
                    System.out.println("URL: " + entry.request().url());
                    System.out.println("Body: " + (entry.request().postData() != null ? entry.request().postData().text() : "N/A"));
                    System.out.println("Headers: " + entry.request().headers());
                    System.out.println("------------------------");

                    postEntries.add(entry);

                    if (entry.response() != null) {
                        System.out.println("-- POST Response --");
                        System.out.println("Status: " + entry.response().status());
                        System.out.println(("Status Text: " + entry.response().statusText()));
                        if (entry.response().headers() != null) {
                            for (HarHeader header : entry.response().headers()) {
                                if (header.name().equals("X-Correlation-Id")) {
                                    System.out.println("X-Correlation-Id: " + header.value());
                                }
                            };
                        }
                        System.out.println("Response Body: " + (
                                entry.response().content() != null &&
                                        entry.response().content().text() !=null ?
                                        entry.response().content().text() :
                                        "N/A"));
                    }
                }
            }
        }
        return postEntries;
    };
}
