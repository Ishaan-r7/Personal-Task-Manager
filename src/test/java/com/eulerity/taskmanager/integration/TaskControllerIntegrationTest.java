package com.eulerity.taskmanager.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eulerity.taskmanager.entity.Task;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.Status;
import com.eulerity.taskmanager.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void postTasksWithValidBodyReturns201() throws Exception {
        String requestBody = """
                {
                  "title": "Finish assignment",
                  "description": "Complete the backend take-home",
                  "dueDate": "2026-05-20",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Finish assignment"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postTasksWithMissingTitleReturns400() throws Exception {
        String requestBody = """
                {
                  "description": "No title provided"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("title: title is required"));
    }

    @Test
    void getTasksReturns200() throws Exception {
        Task task = new Task();
        task.setTitle("Existing task");
        task.setDescription("Already saved");
        task.setDueDate(LocalDate.of(2026, 5, 21));
        task.setPriority(Priority.MEDIUM);
        task.setStatus(Status.IN_PROGRESS);
        taskRepository.save(task);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Existing task"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void getTaskByIdReturns200ForExistingTask() throws Exception {
        Task task = new Task();
        task.setTitle("Fetch me");
        task.setDescription("Lookup by id");
        task.setDueDate(LocalDate.of(2026, 5, 22));
        task.setPriority(Priority.LOW);
        task.setStatus(Status.TODO);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Fetch me"));
    }

    @Test
    void getTaskByIdReturns404ForMissingTask() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found with id: 9999"));
    }

    @Test
    void putTaskReturns200WithUpdatedFields() throws Exception {
        Task task = new Task();
        task.setTitle("Initial title");
        task.setDescription("Initial description");
        task.setDueDate(LocalDate.of(2026, 5, 23));
        task.setPriority(Priority.LOW);
        task.setStatus(Status.TODO);
        Task savedTask = taskRepository.save(task);

        String requestBody = """
                {
                  "title": "Updated title",
                  "priority": "HIGH",
                  "status": "DONE"
                }
                """;

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Initial description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deleteTaskReturns204() throws Exception {
        Task task = new Task();
        task.setTitle("Delete task");
        task.setDescription("To be removed");
        task.setPriority(Priority.MEDIUM);
        task.setStatus(Status.TODO);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNoContent());
    }
}
