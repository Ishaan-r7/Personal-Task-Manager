package com.eulerity.taskmanager.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eulerity.taskmanager.dto.ai.TaskSuggestionResponse;
import com.eulerity.taskmanager.service.AiTaskSuggestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class AiTaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiTaskSuggestionService aiTaskSuggestionService;

    @Test
    void postTasksSuggestWithValidDescriptionReturns200WithCorrectJsonBody() throws Exception {
        TaskSuggestionResponse response = new TaskSuggestionResponse(
                "Submit quarterly report",
                "Prepare and submit the quarterly report before Friday",
                "2026-05-22",
                "HIGH",
                "TODO"
        );

        when(aiTaskSuggestionService.suggest(anyString())).thenReturn(response);

        String requestBody = """
                {
                  "description": "remind me to submit the quarterly report before Friday"
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit quarterly report"))
                .andExpect(jsonPath("$.description").value("Prepare and submit the quarterly report before Friday"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-22"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postTasksSuggestWithBlankDescriptionReturns400() throws Exception {
        String requestBody = """
                {
                  "description": ""
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("description: description is required"));
    }
}
