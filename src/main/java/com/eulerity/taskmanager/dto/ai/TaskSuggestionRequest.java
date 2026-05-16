package com.eulerity.taskmanager.dto.ai;

import jakarta.validation.constraints.NotBlank;

public class TaskSuggestionRequest {

    @NotBlank(message = "description is required")
    private String description;

    public TaskSuggestionRequest() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
