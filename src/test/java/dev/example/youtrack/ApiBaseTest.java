package dev.example.youtrack;

import dev.example.youtrack.endpoints.ApiEndpoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@DisplayName("YouTrack API availability")
class ApiBaseTest extends ApiTestSupport {
    @Test
    @DisplayName("GET issues responds successfully with the configured token")
    void apiIsAvailable() {
        given()
                .spec(requestSpec())
                .queryParam("fields", "id")
                .get(ApiEndpoints.ISSUES)
                .then().spec(responseSpec(200));
    }
}
