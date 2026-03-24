package com.prototype.api_test_suite.util;

import com.prototype.api_test_suite.ApiBaseTest;
import com.prototype.api_test_suite.model.*;
import com.prototype.api_test_suite.service.HarParserService;
import com.prototype.api_test_suite.service.HarReplayService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HarReplayServiceTest extends ApiBaseTest {

    @Mock
    private HarParserService harParserService;

    private HarReplayService harReplayService;

    @Test
    public void shouldReplaySinglePostRequestSuccessfully() {
        // Set up
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        harParserService = new HarParserService(objectMapper);
        harReplayService = new HarReplayService(objectMapper);

        // GIVEN a manually created List<HarRequests>
        UUID testId = UUID.randomUUID();
        String originalCorrelationId = UUID.randomUUID().toString();
        String requestBody = "{\"id\":\"" + testId + "\", \"resultLabel\":\"TEST_LABEL\"}";

        // 1. Create Mock HarHeader for the original Response
        HarHeader mockResponseCorrelationHeader = new HarHeader("X-Correlation-ID", originalCorrelationId);
        List<HarHeader> mockResponseHeaders = Collections.singletonList(mockResponseCorrelationHeader);

        // 2. Create Mock HarResponse (with headers containing Correlation ID)
        HarResponse mockHarResponse = new HarResponse(
                202, // Simulate the original response status
                "Accepted",
                mockResponseHeaders, // Include the mock header here
                null // content for now, not critical for this test's focus
        );

        // 3. Create Mock HarRequest (as before, but include Content-Type header for realism)
        List<HarHeader> mockRequestHeaders = new ArrayList<>();
        mockRequestHeaders.add(new HarHeader("Content-Type", "application/json"));
        mockRequestHeaders.add(new HarHeader("Accept", "application/json")); // Adding for more realistic HAR requests

        HarRequest mockHarRequest = new HarRequest(
                "POST",
                "http://localhost:8081/queue/hearing-results",
                mockRequestHeaders, // Pass the request headers here
                new HarPostData("application/json", requestBody)
        );

        // 4. Create Mock HarEntry (combining request and response)
        HarEntry mockHarEntry = new HarEntry(
                "2023-03-15T10:00:00.000Z", // Example startedDateTime
                mockHarRequest,
                mockHarResponse // <--- The HarEntry now contains the HarResponse with its correlation ID
        );

        System.out.println(mockHarEntry.response().headers());

        // 5. Create the List<HarEntry> to pass to the service
        List<HarEntry> mockHarEntries = Collections.singletonList(mockHarEntry);

        // WHEN the HarReplayService executes the mock Requests
        List<HarReplayResult> replayResults = harReplayService.replayHarRequests(mockHarEntries);

        // THEN it should have one result
        assertThat(replayResults).hasSize(1);

        // AND the result should indicate a 202 status for the POST
        HarReplayResult firstResult = replayResults.get(0);
        assertThat(firstResult.getHttpStatus()).isEqualTo(202);
        assertThat(firstResult.getCorrelationId()).isNotNull();
        assertThat(firstResult.getOriginalCorrelationId()).isEqualTo(originalCorrelationId);
        assertThat(firstResult.getOriginalRequest().url()).contains("localhost:8081");
        assertThat(firstResult.getOriginalRequest().postData().text()).contains(testId.toString());
    }
}
