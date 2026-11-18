package dev.example.youtrack.endpoints;

public final class ApiEndpoints {
    public static final String PROJECTS = "/api/admin/projects";
    public static final String ISSUES = "/api/issues";
    public static final String ISSUE_BY_READABLE_ID = ISSUES + "/{idReadable}";

    private ApiEndpoints() {
    }
}
