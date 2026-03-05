package com.prototype.api_test_suite.lifecycle;

import com.prototype.api_test_suite.ApiBaseTest;
import com.prototype.api_test_suite.dto.HearingResultDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

public class HearingResultLifecycleTest extends ApiBaseTest {

    @Test
    public void postHearingResultToCommandMockApi_ShouldReturnDbEntity_WhenReadApiQueried() {
        // GIVEN a HearingRequestDTO
        UUID testId = UUID.randomUUID();
        HearingResultDTO dto = new HearingResultDTO(
                testId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Result Level",
                "Test Result Label"
        );
        // AND an API request is sent to the Command Mock API containing the DTO in the Request Body
        given()
                .spec(commandSpec)
                .body(dto)
                .post("/hearing-results")
                .then()
                    .statusCode(202);;

        // WHEN the database is queried with the supplied id
        // THEN the response should match the test DTO
        await()
                .atMost(5, SECONDS)
                .pollInterval(500, MILLISECONDS)
                .untilAsserted(() -> {
            given()
                        .spec(querySpec)
                        .pathParam("id", testId)
                    .when()
                        .get("/hearing-results/{id}")
                    .then()
                        .statusCode(200)
                        .body("id", equalTo(testId.toString()))
                        .body("resultLevel", equalTo("Test Result Level"))
                        .body("resultLabel", equalTo("Test Result Label"));
                }

        );
    }
}
