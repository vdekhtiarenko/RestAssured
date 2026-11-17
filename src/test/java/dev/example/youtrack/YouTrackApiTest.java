package dev.example.youtrack;

import dev.example.youtrack.config.TestConfiguration;
import dev.example.youtrack.dto.IssueCreateRequest;
import dev.example.youtrack.dto.IssueResponse;
import dev.example.youtrack.dto.ProjectRef;
import dev.example.youtrack.dto.ProjectResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("YouTrack REST API")
class YouTrackApiTest {
    private static final String PROJECT_FIELDS = "id,name,shortName";
    private static final String ISSUE_FIELDS = "id,idReadable,summary,description,project(id,shortName)";
    private static String token;
    private static String projectId;

    @BeforeAll
    static void configureApi() {
        TestConfiguration configuration = TestConfiguration.load();
        token = configuration.token();
        RestAssured.baseURI = configuration.baseUrl();
        ProjectResponse[] projects = given()
                .header("Authorization", "Bearer " + token)
                .accept(ContentType.JSON)
                .queryParam("fields", PROJECT_FIELDS)
                .get("/api/admin/projects")
                .then().statusCode(200)
                .extract().as(ProjectResponse[].class);

        projectId = Stream.of(projects)
                .filter(project -> "DEMO".equals(project.getShortName()))
                .map(ProjectResponse::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Project with shortName=DEMO was not returned"));
    }

    @Test
    @DisplayName("GET issues returns a successful list")
    void listsIssuesWithValidToken() {
        Response response = given()
                .header("Authorization", bearer(token))
                .accept(ContentType.JSON)
                .queryParam("fields", ISSUE_FIELDS)
                .get("/api/issues");

        assertEquals(200, response.statusCode());
        assertNotNull(response.as(IssueResponse[].class));
    }

    @Test
    @DisplayName("POST issue creates a task and GET retrieves its fields by idReadable")
    void createsAndReadsIssue() {
        String uniqueSummary = "RestAssured create/read " + UUID.randomUUID();
        IssueCreateRequest request = new IssueCreateRequest(uniqueSummary,
                "Created by the YouTrack RestAssured integration suite", new ProjectRef(projectId));

        IssueResponse created = given()
                .header("Authorization", bearer(token))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("fields", ISSUE_FIELDS)
                .body(request)
                .post("/api/issues")
                .then().statusCode(200)
                .extract().as(IssueResponse.class);

        assertNotNull(created.getIdReadable(), "YouTrack should return idReadable for the created issue");
        assertEquals(uniqueSummary, created.getSummary());
        assertEquals(projectId, created.getProject().getId());

        IssueResponse fetched = given()
                .header("Authorization", bearer(token))
                .accept(ContentType.JSON)
                .queryParam("fields", ISSUE_FIELDS)
                .get("/api/issues/{idReadable}", created.getIdReadable())
                .then().statusCode(200)
                .extract().as(IssueResponse.class);

        assertEquals(created.getIdReadable(), fetched.getIdReadable());
        assertEquals(uniqueSummary, fetched.getSummary());
        assertEquals(request.getDescription(), fetched.getDescription());
        System.out.printf("Created YouTrack issue %s: %s%n", created.getIdReadable(), uniqueSummary);
    }

    static Stream<CreationCase> issueCreationCases() {
        return Stream.of(
                new CreationCase("valid issue", new IssueCreateRequest("parameterized create " + UUID.randomUUID(),
                        "Created from MethodSource", new ProjectRef("resolved-at-runtime")), 200),
                new CreationCase("missing summary", new IssueCreateRequest(null, "Summary intentionally omitted",
                        new ProjectRef("resolved-at-runtime")), 400),
                new CreationCase("invalid project id", new IssueCreateRequest("invalid project " + UUID.randomUUID(),
                        "Project ID intentionally invalid", new ProjectRef("not-a-project-id")), 400)
        );
    }

    @ParameterizedTest(name = "POST issue: {0}")
    @MethodSource("issueCreationCases")
    void createsIssueAccordingToScenario(CreationCase scenario) {
        IssueCreateRequest source = scenario.request();
        IssueCreateRequest request = new IssueCreateRequest(source.getSummary(), source.getDescription(),
                new ProjectRef(projectId));
        if ("invalid project id".equals(scenario.name())) {
            request = new IssueCreateRequest(source.getSummary(), source.getDescription(),
                    new ProjectRef("not-a-project-id"));
        }

        Response response = given()
                .header("Authorization", bearer(token))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("fields", ISSUE_FIELDS)
                .body(request)
                .post("/api/issues");

        if (scenario.expectedStatus() == 200) {
            assertEquals(scenario.expectedStatus(), response.statusCode(), response.asString());
            IssueResponse issue = response.as(IssueResponse.class);
            assertTrue(issue.getSummary().startsWith("parameterized create "));
            System.out.printf("Created YouTrack issue %s: %s%n", issue.getIdReadable(), issue.getSummary());
        } else {
            assertEquals(scenario.expectedStatus(), response.statusCode(), response.asString());
            assertFalse(response.asString().isBlank(), "YouTrack should explain the rejected request");
        }
    }

    @Test
    @DisplayName("GET issues rejects an invalid permanent token")
    void rejectsInvalidToken() {
        Response response = given()
                .header("Authorization", bearer("invalid-" + UUID.randomUUID()))
                .accept(ContentType.JSON)
                .queryParam("fields", ISSUE_FIELDS)
                .get("/api/issues");

        assertEquals(401, response.statusCode(), response.asString());
    }

    private static String bearer(String value) {
        return "Bearer " + value;
    }

    record CreationCase(String name, IssueCreateRequest request, int expectedStatus) {
        @Override
        public String toString() {
            return name;
        }
    }
}
