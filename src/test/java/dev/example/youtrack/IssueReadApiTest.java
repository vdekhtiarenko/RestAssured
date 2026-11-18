package dev.example.youtrack;

import dev.example.youtrack.dto.IssueCreateRequest;
import dev.example.youtrack.dto.IssueResponse;
import dev.example.youtrack.dto.ProjectRef;
import dev.example.youtrack.endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("YouTrack issue read endpoints")
class IssueReadApiTest extends BaseTest {
    private static final String ISSUE_FIELDS = "id,idReadable,summary,description,project(id,shortName)";

    @Test
    @DisplayName("GET issues returns a successful list")
    void listsIssuesWithValidToken() {
        IssueResponse[] issues = given()
                .spec(requestSpec())
                .queryParam("fields", ISSUE_FIELDS)
                .get(ApiEndpoints.ISSUES)
                .then().spec(responseSpec(200))
                .extract().as(IssueResponse[].class);

        assertNotNull(issues);
    }

    @Test
    @DisplayName("GET issue returns its fields by idReadable")
    void readsIssueByReadableId() {
        String summary = "RestAssured read " + UUID.randomUUID();
        IssueCreateRequest request = new IssueCreateRequest(summary,
                "Created as a fixture for the issue read scenario", new ProjectRef(projectId()));

        IssueResponse created = requestWithDto(request)
                .queryParam("fields", ISSUE_FIELDS)
                .post(ApiEndpoints.ISSUES)
                .then().spec(responseSpec(200))
                .extract().as(IssueResponse.class);

        Response response = given()
                .spec(requestSpec())
                .queryParam("fields", ISSUE_FIELDS)
                .get(ApiEndpoints.ISSUE_BY_READABLE_ID, created.getIdReadable());
        IssueResponse fetched = response.then().spec(responseSpec(200))
                .extract().as(IssueResponse.class);

        assertEquals(created.getIdReadable(), fetched.getIdReadable());
        assertEquals(summary, fetched.getSummary());
        assertEquals(request.getDescription(), fetched.getDescription());
    }
}
