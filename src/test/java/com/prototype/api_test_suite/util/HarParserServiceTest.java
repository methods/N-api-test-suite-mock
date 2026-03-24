package com.prototype.api_test_suite.util;

import com.prototype.api_test_suite.model.HarEntry;
import com.prototype.api_test_suite.model.HarRequest;
import com.prototype.api_test_suite.service.HarParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HarParserServiceTest {

    private HarParserService harParserService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        harParserService = new HarParserService(objectMapper);
    }

    @Test
    public void shouldParseSinglePostRequestFromHar() throws IOException {
        // GIVEN a HAR file with an Entry field with a single POST request
        URL resource = getClass().getClassLoader().getResource("single-post-HAR-for-test.har");
        assertThat(resource).isNotNull();

        String harFilePath = new File(resource.getFile()).getAbsolutePath();

        // WHEN the HarParserService processes it
        List<HarEntry> entries = harParserService.extractPostRequests(harFilePath);

        // THEN it should find exactly one HAR request
        assertThat(entries).isNotNull().hasSize(1);

        // AND that entry should contain a POST request
        HarEntry firstEntry = entries.get(0);
        assertThat(firstEntry.request()).isNotNull();

        // AND that request should have the correct details
        assertThat(firstEntry.request().method()).isEqualTo("POST");
        assertThat(firstEntry.request().url()).isEqualTo( "http://localhost:8081/queue/hearing-results");

        assertThat(firstEntry.request().postData()).isNotNull();
        assertThat(firstEntry.request().postData().text()).isEqualTo("{\"id\":\"d1e2f3a4-b5c6-7890-1234-56789abcdef0\",\"offenceId\":\"e1f2a3b4-c5d6-7890-1234-56789abcdef1\",\"caseId\":\"f1a2b3c4-d5e6-7890-1234-56789abcdef2\",\"resultLevel\":\"TEST_LEVEL\",\"resultLabel\":\"TEST_LABEL\"}");
    }
}
