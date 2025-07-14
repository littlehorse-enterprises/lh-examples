package io.littlehorse.resources;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testVerifyIdentityEndpoint() {
        given().when()
                .get("/identity/verify")
                .then()
                .statusCode(200);
    }
}
