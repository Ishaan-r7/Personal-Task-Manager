package com.eulerity.taskmanager.service;

import com.eulerity.taskmanager.dto.ai.TaskSuggestionResponse;

public interface AiTaskSuggestionService {

    TaskSuggestionResponse suggest(String description);
}
