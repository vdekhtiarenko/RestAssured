package dev.example.youtrack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueCreateRequest {
    private String summary;
    private String description;
    private ProjectRef project;

    public IssueCreateRequest() { }

    public IssueCreateRequest(String summary, String description, ProjectRef project) {
        this.summary = summary;
        this.description = description;
        this.project = project;
    }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public ProjectRef getProject() { return project; }

    public void setProject(ProjectRef project) { this.project = project; }
}
