package dev.example.youtrack;

import dev.example.youtrack.dto.IssueCreateRequest;
import dev.example.youtrack.dto.IssueResponse;
import dev.example.youtrack.dto.ProjectRef;
import dev.example.youtrack.dto.ProjectResponse;
import dev.example.youtrack.endpoints.ApiEndpoints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("YouTrack API availability")
class ApiBaseTest extends ApiTestSupport {
    @Test
    @DisplayName("GET projects returns the configured DEMO project")
    void projectsEndpointIsAvailable() {
        ProjectResponse[] projects = given()
                .spec(requestSpec())
                .queryParam("fields", "id,name,shortName")
                .get(ApiEndpoints.PROJECTS)
                .then().spec(responseSpec(200))
                .extract().as(ProjectResponse[].class);

        assertTrue(Arrays.stream(projects)
                .anyMatch(project -> "DEMO".equals(project.getShortName())),
                "Project with shortName=DEMO should be present");
    }

    @Test
    @DisplayName("GET issues responds successfully with the configured token")
    void issuesEndpointIsAvailable() {
        IssueResponse[] issues = given()
                .spec(requestSpec())
                .queryParam("fields", "id,idReadable,summary")
                .get(ApiEndpoints.ISSUES)
                .then().spec(responseSpec(200))
                .extract().as(IssueResponse[].class);

        assertNotNull(issues);
    }

    @Test
    @DisplayName("GET issue by readable ID responds successfully")
    void issueByReadableIdEndpointIsAvailable() {
        String summary = "ApiBase endpoint check " + UUID.randomUUID();
        IssueCreateRequest request = new IssueCreateRequest(summary,
                "Created as a fixture for ApiBaseTest", new ProjectRef(projectId()));

        IssueResponse created = requestWithDto(request)
                .queryParam("fields", "id,idReadable,summary")
                .post(ApiEndpoints.ISSUES)
                .then().spec(responseSpec(200))
                .extract().as(IssueResponse.class);

        IssueResponse fetched = given()
                .spec(requestSpec())
                .queryParam("fields", "id,idReadable,summary")
                .get(ApiEndpoints.ISSUE_BY_READABLE_ID, created.getIdReadable())
                .then().spec(responseSpec(200))
                .extract().as(IssueResponse.class);

        assertEquals(created.getIdReadable(), fetched.getIdReadable());
        assertEquals(summary, fetched.getSummary());
    }
}
