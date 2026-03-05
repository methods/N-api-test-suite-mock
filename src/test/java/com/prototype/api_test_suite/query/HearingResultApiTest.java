package com.prototype.api_test_suite.query;

import com.prototype.api_test_suite.ApiBaseTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class HearingResultApiTest extends ApiBaseTest {

    @Test
    public void testGetHearingResultById_ShouldReturnHearingResult_WhenExists() {
        // GIVEN a known hearing result ID in the database
        String id = "a1b2c3d4-e5f6-4789-8123-456789abcdef";

        // WHEN the endpoint is called
        // THEN the response should match expectations
        given()
                .spec(querySpec)
                .pathParam("id", id)
                .when()
                .get("/hearing-results/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("resultLevel", notNullValue())
                .body("resultLabel", notNullValue());
    }
}
