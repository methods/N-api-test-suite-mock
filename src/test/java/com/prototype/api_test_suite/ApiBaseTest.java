package com.prototype.api_test_suite;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public abstract class ApiBaseTest {
    // Request spec templates for the mock / prototype API test suite
    protected static RequestSpecification commandSpec;
    protected static RequestSpecification querySpec;

    @BeforeAll
    public static void setUp() {
        // API request spec for the Command Mock API
        commandSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:8081")
                .setBasePath("/queue")
                .setContentType(ContentType.JSON)
                .build();

        // API request spec for the Read prototype API
        querySpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:8080")
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .build();
    }
}
