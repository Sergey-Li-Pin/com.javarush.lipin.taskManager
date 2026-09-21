package com.javarush.lipin.taskmanager.service;

import com.javarush.lipin.taskmanager.dto.TaskMapper;
import com.javarush.lipin.taskmanager.dto.TaskResponse;
import com.javarush.lipin.taskmanager.exception.ResourceNotFoundException;
import com.javarush.lipin.taskmanager.model.entity.Task;
import com.javarush.lipin.taskmanager.model.entity.TaskStatus;
import com.javarush.lipin.taskmanager.model.entity.User;
import com.javarush.lipin.taskmanager.model.repository.TaskRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private User owner;

    @BeforeEach
    void setUp() {
        MeterRegistry registry = new SimpleMeterRegistry();
        taskService = new TaskService(taskRepository, new TaskMapper(),
                new TaskMetrics(registry), registry);

        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
    }

    @Test
    void getAllForUser_returnsOnlyOwnTasks() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("My task");
        task.setStatus(TaskStatus.NEW);
        task.setOwner(owner);
        when(taskRepository.findAllByOwnerId(1L)).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAllForUser(owner, null);

        assertEquals(1, result.size());
        assertEquals("My task", result.get(0).title());
    }

    @Test
    void getById_throws404_whenTaskBelongsToAnotherUser() {
        // Чужая задача: репозиторий по (id + ownerId) ничего не находит
        when(taskRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getByIdForUser(10L, owner));
    }

    @Test
    void delete_deletesOnlyOwnTask() {
        Task task = new Task();
        task.setId(10L);
        task.setOwner(owner);
        when(taskRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(task));

        taskService.delete(10L, owner);

        verify(taskRepository).delete(task);
    }

    @Test
    void delete_throws404_whenTaskNotOwned() {
        when(taskRepository.findByIdAndOwnerId(any(), any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.delete(10L, owner));
    }
}