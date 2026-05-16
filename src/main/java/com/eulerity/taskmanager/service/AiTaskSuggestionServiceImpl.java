package com.eulerity.taskmanager.service;

import com.eulerity.taskmanager.dto.ai.TaskSuggestionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiTaskSuggestionServiceImpl implements AiTaskSuggestionService {

    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public AiTaskSuggestionServiceImpl(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    @Override
    public TaskSuggestionResponse suggest(String description) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        String prompt = """
                You are a task management assistant. Convert the following plain-language description into a structured task.
                Return ONLY valid JSON with absolutely no markdown, no code fences, and no explanation outside the JSON.
                Use exactly these fields:
                {
                  "title": "short task title",
                  "description": "fuller description",
                  "dueDate": "YYYY-MM-DD or null",
                  "priority": "LOW or MEDIUM or HIGH",
                  "status": "TODO"
                }
                User input: %s
                """.formatted(description);

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", new Object[] {
                            Map.of(
                                    "parts", new Object[] {
                                            Map.of("text", prompt)
                                    }
                            )
                    }
            ));

            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL_TEMPLATE.formatted(encodedApiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Gemini API request failed with status " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String responseText = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText();

            String cleanedResponse = stripMarkdownCodeFences(responseText);
            return objectMapper.readValue(cleanedResponse, TaskSuggestionResponse.class);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to parse AI response into structured task JSON", exception);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to generate task suggestion from AI provider", exception);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to generate task suggestion from AI provider", exception);
        }
    }

    private String stripMarkdownCodeFences(String response) {
        if (response == null) {
            return "";
        }

        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        return cleaned;
    }
}
