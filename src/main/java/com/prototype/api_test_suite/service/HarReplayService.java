package com.prototype.api_test_suite.service;

import com.prototype.api_test_suite.model.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Service
public class HarReplayService {

    private ObjectMapper objectMapper;
    private HarParserService harParserService;
    private final RequestSpecification baseRequestSpec;

    public HarReplayService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.baseRequestSpec = new RequestSpecBuilder()
                .log(LogDetail.ALL)
                .build();
        }

    public List<HarReplayResult> replayHarRequests(List<HarEntry> harEntries) {

        List<HarReplayResult> replayResults = new ArrayList<>();

        for (HarEntry entry : harEntries) {
            HarRequest request = entry.request();
            HarResponse originalResponse = entry.response();
            System.out.println("Original Response: " + originalResponse);
            String method = request.method();
            String url = request.url();
            String originalBody = "";
            if (request.postData() != null) {
                originalBody = request.postData().text();
            }
            System.out.println(" HAR replay Original request body: " + originalBody);
            String modifiedBody = originalBody;

            if (!originalBody.isEmpty()) {
                ObjectNode objectNode = null;
                JsonNode jsonNode = objectMapper.readTree(originalBody);
                objectNode = (ObjectNode) jsonNode;

                String originalHarId = objectNode.has("id") ? objectNode.get("id").asText() : "N/A";
                UUID replayHarId = UUID.randomUUID();
                objectNode.put("id", replayHarId.toString());
                objectNode.put("resultLabel", "[REPLAY-" + replayHarId.toString().substring(0, 8) + "] (Original HAR ID: " + originalHarId + ")");
                modifiedBody = objectMapper.writeValueAsString(objectNode);
            }

            System.out.println(" Modified request body: " + modifiedBody);

            // Construct the API call from the HAR headers

            RequestSpecification currentRequest = given().spec(this.baseRequestSpec);

            if (request.headers() != null && !request.headers().isEmpty()) {
                for (HarHeader header : request.headers()) {
                    String headerName = header.name();
                    // Exclude Content-Length and Content-Type from manual addition
                    if (!"content-length".equalsIgnoreCase(headerName) &&
                            !"content-type".equalsIgnoreCase(headerName)) {
                        currentRequest.header(headerName, header.value());
                    }
                }
                System.out.println(" Request header of the request to be replayed: " + request.headers());
            }

            String harContentType = request.headers().stream()
                    .filter(h -> "content-type".equalsIgnoreCase(h.name()))
                    .map(HarHeader::value)
                    .findFirst()
                    .orElse(null);
            System.out.println(" harContentType: " + harContentType);

            if (harContentType != null) {
                currentRequest.contentType(harContentType);
            } else if (!modifiedBody.isEmpty()) {
                currentRequest.contentType(ContentType.JSON);
            }
            System.out.println(" currentRequest: " + currentRequest);

            if ("POST".equals(method)) {
                Response response = given()
                        .spec(currentRequest)
                        .body(modifiedBody)
                        .when()
                        .post(url);
                HarReplayResult result = new HarReplayResult();
                result.setOriginalRequest(request);
                result.setHttpStatus(response.getStatusCode());
                result.setResponseBody(response.getBody().asString());
                result.setCorrelationId(response.getHeader("X-Correlation-ID"));
                result.setOriginalCorrelationId(null);

                // Get the original correlation Id
                if (originalResponse.headers() != null) {
                    for (HarHeader header : originalResponse.headers()) {
                        if (header.name() != null && "X-Correlation-ID".equalsIgnoreCase(header.name())) {
                            System.out.println("Original Correlation Id check: " + header.value());
                            result.setOriginalCorrelationId(header.value());
                        }
                    };
                }

                replayResults.add(result);
            }
        };

        return replayResults;
    };
}
