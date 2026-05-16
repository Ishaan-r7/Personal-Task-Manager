package com.eulerity.taskmanager.controller;

import com.eulerity.taskmanager.dto.ai.TaskSuggestionRequest;
import com.eulerity.taskmanager.dto.ai.TaskSuggestionResponse;
import com.eulerity.taskmanager.service.AiTaskSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class AiTaskController {

    private final AiTaskSuggestionService aiTaskSuggestionService;

    public AiTaskController(AiTaskSuggestionService aiTaskSuggestionService) {
        this.aiTaskSuggestionService = aiTaskSuggestionService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<TaskSuggestionResponse> suggestTask(@Valid @RequestBody TaskSuggestionRequest request) {
        return ResponseEntity.ok(aiTaskSuggestionService.suggest(request.getDescription()));
    }
}
