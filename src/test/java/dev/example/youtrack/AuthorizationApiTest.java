package dev.example.youtrack;

import dev.example.youtrack.endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("YouTrack API authorization")
class AuthorizationApiTest extends BaseTest {
    @Test
    @DisplayName("GET issues rejects an invalid permanent token")
    void rejectsInvalidToken() {
        Response response = given()
                .spec(requestSpec("invalid-" + UUID.randomUUID()))
                .queryParam("fields", "id,idReadable,summary")
                .get(ApiEndpoints.ISSUES);

        assertEquals(401, response.statusCode(), response.asString());
    }
}
