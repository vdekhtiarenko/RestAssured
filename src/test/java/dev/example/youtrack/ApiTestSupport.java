package dev.example.youtrack;

import dev.example.youtrack.config.ApiSpecifications;
import dev.example.youtrack.config.TestConfiguration;
import dev.example.youtrack.dto.ProjectResponse;
import dev.example.youtrack.endpoints.ApiEndpoints;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;

abstract class ApiTestSupport {
    private static TestConfiguration configuration;
    private static String projectId;

    @BeforeAll
    static void configureApi() {
        configuration = TestConfiguration.load();
    }

    protected static RequestSpecification requestSpec() {
        return requestSpec(configuration.token());
    }

    protected static RequestSpecification requestSpec(String token) {
        return ApiSpecifications.request(configuration.baseUrl(), token);
    }

    protected static ResponseSpecification responseSpec(int statusCode) {
        return ApiSpecifications.response(statusCode);
    }

    protected static RequestSpecification requestWithDto(Object requestDto) {
        return given().spec(requestSpec()).body(requestDto);
    }

    protected static <T> T responseAsDto(Response response, Class<T> dtoType) {
        return response.as(dtoType);
    }

    protected static synchronized String projectId() {
        if (projectId == null) {
            ProjectResponse[] projects = given()
                    .spec(requestSpec())
                    .queryParam("fields", "id,name,shortName")
                    .get(ApiEndpoints.PROJECTS)
                    .then().spec(responseSpec(200))
                    .extract().as(ProjectResponse[].class);

            projectId = java.util.Arrays.stream(projects)
                    .filter(project -> "DEMO".equals(project.getShortName()))
                    .map(ProjectResponse::getId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Project with shortName=DEMO was not returned"));
        }
        return projectId;
    }
}
