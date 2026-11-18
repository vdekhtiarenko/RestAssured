package dev.example.youtrack.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesIssueCreateRequestAndOmitsNullFields() throws Exception {
        IssueCreateRequest request = new IssueCreateRequest(
                "Example issue", null, new ProjectRef("0-1"));

        String json = objectMapper.writeValueAsString(request);

        assertEquals("{\"summary\":\"Example issue\",\"project\":{\"id\":\"0-1\"}}", json);
    }

    @Test
    void deserializesIssueResponseAndIgnoresUnknownFields() throws Exception {
        String json = """
                {
                  "id": "123-1",
                  "idReadable": "DEMO-1",
                  "summary": "Example issue",
                  "description": "Issue description",
                  "project": {"id": "0-1", "shortName": "DEMO", "$type": "Project"},
                  "$type": "Issue"
                }
                """;

        IssueResponse response = objectMapper.readValue(json, IssueResponse.class);

        assertEquals("123-1", response.getId());
        assertEquals("DEMO-1", response.getIdReadable());
        assertEquals("Example issue", response.getSummary());
        assertEquals("Issue description", response.getDescription());
        assertEquals("0-1", response.getProject().getId());
    }

    @Test
    void deserializesProjectResponseAndIgnoresUnknownFields() throws Exception {
        String json = """
                {"id":"0-1","name":"Demo project","shortName":"DEMO","$type":"Project"}
                """;

        ProjectResponse response = objectMapper.readValue(json, ProjectResponse.class);

        assertEquals("0-1", response.getId());
        assertEquals("Demo project", response.getName());
        assertEquals("DEMO", response.getShortName());
    }
}
