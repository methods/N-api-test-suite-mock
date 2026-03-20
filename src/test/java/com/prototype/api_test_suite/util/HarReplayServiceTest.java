package com.prototype.api_test_suite.util;

import com.prototype.api_test_suite.ApiBaseTest;
import com.prototype.api_test_suite.model.HarPostData;
import com.prototype.api_test_suite.model.HarReplayResult;
import com.prototype.api_test_suite.model.HarRequest;
import com.prototype.api_test_suite.service.HarParserService;
import com.prototype.api_test_suite.service.HarReplayService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.databind.ObjectMapper;

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
        String requestBody = "{\"id\":\"" + testId + "\", \"resultLabel\":\"TEST_LABEL\"}";

        HarRequest mockHarRequest = new HarRequest(
                "POST",
                "http://localhost:8081/queue/hearing-results",
                Collections.emptyList(), // Simplified for test
                new HarPostData("application/json", requestBody)
        );
        List<HarRequest> mockHarRequests = Collections.singletonList(mockHarRequest);

        // WHEN the HarReplayService executes the mock Requests
        List<HarReplayResult> replayResults = harReplayService.replayHarRequests(mockHarRequests);

        // THEN it should have one result
        assertThat(replayResults).hasSize(1);

        // AND the result should indicate a 202 status for the POST
        HarReplayResult firstResult = replayResults.get(0);
        assertThat(firstResult.getHttpStatus()).isEqualTo(202);
        assertThat(firstResult.getCorrelationId()).isNotNull();
        assertThat(firstResult.getOriginalRequest().url()).contains("localhost:8081");
        assertThat(firstResult.getOriginalRequest().postData().text()).contains(testId.toString());
    }
}
