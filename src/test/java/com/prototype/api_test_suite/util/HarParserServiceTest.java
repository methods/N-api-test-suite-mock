package com.prototype.api_test_suite.util;

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

@SpringBootTest
public class HarParserServiceTest {

    private HarParserService harParserService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        harParserService = new HarParserService(objectMapper);
    }

    @Test
    public void shouldParseSinglePostRequestFromHar() throws IOException {
        // GIVEN a HAR file with a single POST request
        URL resource = getClass().getClassLoader().getResource("single-post-HAR-for-test.har");
        assertThat(resource).isNotNull();

        String harFilePath = new File(resource.getFile()).getAbsolutePath();

        // WHEN the HarParserService processes it
        List<HarRequest> requests = harParserService.extractPostRequests(harFilePath);

        // THEN it should find exactly one HAR request
        assertThat(requests).isNotNull().hasSize(1);

        // AND that request should have the correct details
        HarRequest postRequest = requests.get(0);
        assertThat(postRequest.method()).isEqualTo("POST");
        assertThat(postRequest.url()).isEqualTo( "http://localhost:8081/queue/hearing-results");

        assertThat(postRequest.postData()).isNotNull();
        assertThat(postRequest.postData().text()).isEqualTo("{\"id\":\"d1e2f3a4-b5c6-7890-1234-56789abcdef0\",\"offenceId\":\"e1f2a3b4-c5d6-7890-1234-56789abcdef1\",\"caseId\":\"f1a2b3c4-d5e6-7890-1234-56789abcdef2\",\"resultLevel\":\"TEST_LEVEL\",\"resultLabel\":\"TEST_LABEL\"}");
    }
}
