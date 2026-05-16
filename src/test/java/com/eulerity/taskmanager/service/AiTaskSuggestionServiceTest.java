package com.eulerity.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.eulerity.taskmanager.dto.ai.TaskSuggestionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AiTaskSuggestionServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private AiTaskSuggestionServiceImpl aiTaskSuggestionService;

    @BeforeEach
    void setUp() {
        aiTaskSuggestionService = new AiTaskSuggestionServiceImpl(new ObjectMapper());
        ReflectionTestUtils.setField(aiTaskSuggestionService, "httpClient", httpClient);
    }

    @Test
    void suggestThrowsIllegalStateExceptionWhenApiKeyIsBlank() {
        ReflectionTestUtils.setField(aiTaskSuggestionService, "apiKey", "");

        assertThrows(IllegalStateException.class, () -> aiTaskSuggestionService.suggest("Plan vacation"));
    }

    @Test
    void suggestParsesValidJsonResponseIntoTaskSuggestionResponse() throws Exception {
        ReflectionTestUtils.setField(aiTaskSuggestionService, "apiKey", "test-key");

        String modelJson = """
                {
                  "title": "Plan vacation",
                  "description": "Book flights and hotels for the summer trip",
                  "dueDate": "2026-06-01",
                  "priority": "MEDIUM",
                  "status": "TODO"
                }
                """;

        String geminiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": %s
                          }
                        ]
                      }
                    }
                  ]
                }
                """.formatted(new ObjectMapper().writeValueAsString(modelJson));

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(geminiResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        TaskSuggestionResponse response = aiTaskSuggestionService.suggest("Plan summer vacation");

        assertEquals("Plan vacation", response.getTitle());
        assertEquals("Book flights and hotels for the summer trip", response.getDescription());
        assertEquals("2026-06-01", response.getDueDate());
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("TODO", response.getStatus());
    }
}
