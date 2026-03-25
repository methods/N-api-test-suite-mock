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

public class HarReplayServiceTest {

    @Mock
    private HarParserService harParserService;

    private HarReplayService harReplayService;

    private HarEntry createMockHarEntry(
            String method,
            String url,
            String requestBody,
            String originalCorrelationId,
            List<HarHeader> requestHeaders,
            int originalResponseStatus) {

        // Mock HarResponse Headers
        List<HarHeader> mockResponseHeaders = originalCorrelationId != null && !originalCorrelationId.isEmpty()
                ? Collections.singletonList(new HarHeader("X-Correlation-ID", originalCorrelationId))
                : Collections.emptyList();

        // Mock HarResponse
        HarResponse mockHarResponse = new HarResponse(
                originalResponseStatus,
                "Accepted", // Simplistic, could be dynamic
                mockResponseHeaders,
                null // content
        );

        // Mock HarRequest PostData
        HarPostData mockPostData = requestBody != null && !requestBody.isEmpty()
                ? new HarPostData("application/json", requestBody)
                : null;

        // Mock HarRequest
        HarRequest mockHarRequest = new HarRequest(
                method,
                url,
                requestHeaders != null ? requestHeaders : Collections.emptyList(),
                mockPostData
        );

        // Return the constructed Mock HarEntry
        return new HarEntry(
                "2023-03-15T10:00:00.000Z",
                mockHarRequest,
                mockHarResponse
        );
    }

    @Test
    public void shouldReplaySinglePostRequestSuccessfully() {
        // Set up
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        harParserService = new HarParserService(objectMapper);
        harReplayService = new HarReplayService(objectMapper);

        UUID testId = UUID.randomUUID();
        String originalCorrelationId = UUID.randomUUID().toString();
        String requestBody = "{\"id\":\"" + testId + "\", \"resultLabel\":\"TEST_LABEL\"}";
        List<HarHeader> requestHeaders = new ArrayList<>();
        requestHeaders.add(new HarHeader("Content-Type", "application/json"));
        requestHeaders.add(new HarHeader("Accept", "application/json, text/plain, */*"));

        // GIVEN a mock HarEntry using the helper
        HarEntry mockHarEntry = createMockHarEntry(
                "POST",
                "http://localhost:8081/queue/hearing-results", // Ensure URL points to WireMock
                requestBody,
                originalCorrelationId,
                requestHeaders,
                202 // Original HAR response status
        );

        List<HarEntry> mockHarEntries = Collections.singletonList(mockHarEntry);

        // WHEN the HarReplayService receives the List<mockHarEntry>
        List<HarReplayResult> replayResults = harReplayService.replayHarRequests(mockHarEntries);

        // THEN it should output one result
        assertThat(replayResults).hasSize(1);

        // AND the result should indicate a 202 status for the POST and match the input
        HarReplayResult firstResult = replayResults.get(0);
        assertThat(firstResult.getHttpStatus()).isEqualTo(202);
        assertThat(firstResult.getCorrelationId()).isNotNull();
        assertThat(firstResult.getOriginalCorrelationId()).isEqualTo(originalCorrelationId);
        assertThat(firstResult.getOriginalRequest().url()).contains("localhost:8081");
        assertThat(firstResult.getOriginalRequest().postData().text()).contains(testId.toString());
    }
}
