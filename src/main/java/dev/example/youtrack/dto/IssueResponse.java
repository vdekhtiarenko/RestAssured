package dev.example.youtrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueResponse {
    private String id;
    private String idReadable;
    private String summary;
    private String description;
    private ProjectRef project;

    public IssueResponse() { }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getIdReadable() { return idReadable; }

    public void setIdReadable(String idReadable) { this.idReadable = idReadable; }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public ProjectRef getProject() { return project; }

    public void setProject(ProjectRef project) { this.project = project; }
}
