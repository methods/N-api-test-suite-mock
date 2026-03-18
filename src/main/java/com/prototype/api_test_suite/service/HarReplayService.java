package com.prototype.api_test_suite.service;

import com.prototype.api_test_suite.model.HarReplayResult;
import com.prototype.api_test_suite.model.HarRequest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Service
public class HarReplayService {

    private ObjectMapper objectMapper;
    private HarParserService harParserService;
    private final RequestSpecification commandSpec;
    private final RequestSpecification querySpec;

    public HarReplayService() {
        // API request spec for the Command Mock API
        this.commandSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:8081")
                .setBasePath("/queue")
                .setContentType(ContentType.JSON)
                .build();

        // API request spec for the Read prototype API
        this.querySpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:8080")
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .build();
    }

    public List<HarReplayResult> replayHarRequests(List<HarRequest> harRequests) {

        List<HarReplayResult> replayResults = new ArrayList<>();

        for (HarRequest request : harRequests) {
            String method = request.method();
            String url = request.url();
            String body = "";
            if (request.postData() != null) {
                body = request.postData().text();
            }

            if("POST".equals(method)) {
                Response response = given()
                        .spec(commandSpec)
                        .body(body)
                        .when()
                            .post(url);
                HarReplayResult result = new HarReplayResult();
                result.setOriginalRequest(request);
                result.setHttpStatus(response.getStatusCode());
                result.setResponseBody(response.getBody().asString());
                result.setCorrelationId(response.getHeader("X-Correlation-ID"));

                replayResults.add(result);
            }
        };

        return replayResults;
    };
}
