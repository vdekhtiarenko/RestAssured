package dev.example.youtrack;

import dev.example.youtrack.dto.IssueCreateRequest;
import dev.example.youtrack.dto.IssueResponse;
import dev.example.youtrack.dto.ProjectRef;
import dev.example.youtrack.endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("YouTrack issue create endpoint")
class IssueCreateApiTest extends ApiTestSupport {
    private static final String ISSUE_FIELDS = "id,idReadable,summary,description,project(id,shortName)";

    static Stream<CreationCase> issueCreationCases() {
        return Stream.of(
                new CreationCase("valid issue", "parameterized create " + UUID.randomUUID(), "resolved-at-runtime", 200),
                new CreationCase("missing summary", null, "resolved-at-runtime", 400),
                new CreationCase("invalid project id", "invalid project " + UUID.randomUUID(), "not-a-project-id", 400)
        );
    }

    @ParameterizedTest(name = "POST issue: {0}")
    @MethodSource("issueCreationCases")
    void createsIssueAccordingToScenario(CreationCase scenario) {
        String projectReference = "resolved-at-runtime".equals(scenario.projectId())
                ? projectId() : scenario.projectId();
        IssueCreateRequest request = new IssueCreateRequest(scenario.summary(), "Created from MethodSource",
                new ProjectRef(projectReference));

        Response response = requestWithDto(request)
                .queryParam("fields", ISSUE_FIELDS)
                .post(ApiEndpoints.ISSUES);

        assertEquals(scenario.expectedStatus(), response.statusCode(), response.asString());
        if (scenario.expectedStatus() == 200) {
            IssueResponse issue = responseAsDto(response, IssueResponse.class);
            assertTrue(issue.getSummary().startsWith("parameterized create "));
        } else {
            assertFalse(response.asString().isBlank(), "YouTrack should explain the rejected request");
        }
    }

    record CreationCase(String name, String summary, String projectId, int expectedStatus) {
        @Override
        public String toString() {
            return name;
        }
    }
}
