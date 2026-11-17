package dev.example.youtrack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRef {
    private String id;

    public ProjectRef() { }

    public ProjectRef(String id) { this.id = id; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }
}
