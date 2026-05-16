package com.eulerity.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eulerity.taskmanager.dto.task.TaskCreateRequest;
import com.eulerity.taskmanager.dto.task.TaskResponse;
import com.eulerity.taskmanager.dto.task.TaskUpdateRequest;
import com.eulerity.taskmanager.entity.Task;
import com.eulerity.taskmanager.exception.TaskNotFoundException;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.Status;
import com.eulerity.taskmanager.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void createTaskReturnsCorrectResponseWithStatusDefaultingToTodo() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Write report");
        request.setDescription("Finish the quarterly report");
        request.setDueDate(LocalDate.of(2026, 5, 20));
        request.setPriority(Priority.HIGH);

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Write report");
        savedTask.setDescription("Finish the quarterly report");
        savedTask.setDueDate(LocalDate.of(2026, 5, 20));
        savedTask.setPriority(Priority.HIGH);
        savedTask.setStatus(Status.TODO);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertEquals(1L, response.getId());
        assertEquals("Write report", response.getTitle());
        assertEquals("Finish the quarterly report", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 20), response.getDueDate());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(Status.TODO, response.getStatus());
    }

    @Test
    void getAllTasksReturnsCorrectlyMappedList() {
        Task taskOne = new Task();
        taskOne.setId(1L);
        taskOne.setTitle("Task One");
        taskOne.setDescription("First task");
        taskOne.setDueDate(LocalDate.of(2026, 5, 21));
        taskOne.setPriority(Priority.LOW);
        taskOne.setStatus(Status.TODO);

        Task taskTwo = new Task();
        taskTwo.setId(2L);
        taskTwo.setTitle("Task Two");
        taskTwo.setDescription("Second task");
        taskTwo.setDueDate(LocalDate.of(2026, 5, 22));
        taskTwo.setPriority(Priority.MEDIUM);
        taskTwo.setStatus(Status.IN_PROGRESS);

        when(taskRepository.findAll()).thenReturn(List.of(taskOne, taskTwo));

        List<TaskResponse> responses = taskService.getAllTasks();

        assertEquals(2, responses.size());
        assertEquals("Task One", responses.get(0).getTitle());
        assertEquals(Priority.LOW, responses.get(0).getPriority());
        assertEquals("Task Two", responses.get(1).getTitle());
        assertEquals(Status.IN_PROGRESS, responses.get(1).getStatus());
    }

    @Test
    void getTaskByIdReturnsCorrectResponseWhenFound() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Read book");
        task.setDescription("Read system design book");
        task.setDueDate(LocalDate.of(2026, 5, 25));
        task.setPriority(Priority.MEDIUM);
        task.setStatus(Status.DONE);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(10L);

        assertEquals(10L, response.getId());
        assertEquals("Read book", response.getTitle());
        assertEquals("Read system design book", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 25), response.getDueDate());
        assertEquals(Priority.MEDIUM, response.getPriority());
        assertEquals(Status.DONE, response.getStatus());
    }

    @Test
    void getTaskByIdThrowsTaskNotFoundExceptionWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(99L));
    }

    @Test
    void updateTaskAppliesOnlyNonNullFields() {
        Task existingTask = new Task();
        existingTask.setId(5L);
        existingTask.setTitle("Original title");
        existingTask.setDescription("Original description");
        existingTask.setDueDate(LocalDate.of(2026, 5, 30));
        existingTask.setPriority(Priority.LOW);
        existingTask.setStatus(Status.TODO);

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Updated title");
        request.setPriority(Priority.HIGH);

        Task savedTask = new Task();
        savedTask.setId(5L);
        savedTask.setTitle("Updated title");
        savedTask.setDescription("Original description");
        savedTask.setDueDate(LocalDate.of(2026, 5, 30));
        savedTask.setPriority(Priority.HIGH);
        savedTask.setStatus(Status.TODO);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.updateTask(5L, request);

        assertEquals("Updated title", response.getTitle());
        assertEquals("Original description", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 30), response.getDueDate());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(Status.TODO, response.getStatus());

        assertEquals("Updated title", existingTask.getTitle());
        assertEquals("Original description", existingTask.getDescription());
        assertEquals(LocalDate.of(2026, 5, 30), existingTask.getDueDate());
        assertEquals(Priority.HIGH, existingTask.getPriority());
        assertEquals(Status.TODO, existingTask.getStatus());
        assertNull(request.getDescription());
    }

    @Test
    void deleteTaskCallsRepositoryDelete() {
        Task existingTask = new Task();
        existingTask.setId(7L);
        existingTask.setTitle("Delete me");

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(7L);

        verify(taskRepository, times(1)).delete(existingTask);
    }
}
