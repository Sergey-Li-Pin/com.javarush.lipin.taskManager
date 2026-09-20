package com.javarush.lipin.taskmanager.service;

import com.javarush.lipin.taskmanager.dto.TaskCreateRequest;
import com.javarush.lipin.taskmanager.dto.TaskMapper;
import com.javarush.lipin.taskmanager.dto.TaskResponse;
import com.javarush.lipin.taskmanager.dto.TaskUpdateRequest;
import com.javarush.lipin.taskmanager.exception.ResourceNotFoundException;
import com.javarush.lipin.taskmanager.model.entity.Task;
import com.javarush.lipin.taskmanager.model.entity.TaskStatus;
import com.javarush.lipin.taskmanager.model.entity.User;
import com.javarush.lipin.taskmanager.model.repository.TaskRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskMetrics taskMetrics;
    private final MeterRegistry meterRegistry;

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllForUser(User currentUser, TaskStatus statusFilter) {
        log.debug("Fetching tasks for user id={}, filter={}", currentUser.getId(), statusFilter);

        List<Task> tasks = (statusFilter == null)
                ? taskRepository.findAllByOwnerId(currentUser.getId())
                : taskRepository.findAllByOwnerIdAndStatus(currentUser.getId(), statusFilter);

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getByIdForUser(Long taskId, User currentUser) {
        return taskMapper.toResponse(findOwnedTask(taskId, currentUser));
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest request, User currentUser) {
        Timer.Sample sample = taskMetrics.startTimer(meterRegistry);
        log.info("User id={} creates task '{}'", currentUser.getId(), request.title());

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setOwner(currentUser);

        Task saved = taskRepository.save(task);
        taskMetrics.onTaskCreated();
        taskMetrics.stopTimer(sample);

        log.info("Task id={} created for user id={}", saved.getId(), currentUser.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskUpdateRequest request, User currentUser) {
        log.info("User id={} updates task id={}", currentUser.getId(), taskId);

        Task task = findOwnedTask(taskId, currentUser);
        taskMapper.updateEntity(task, request);

        Task saved = taskRepository.save(task);
        log.info("Task id={} updated", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long taskId, User currentUser) {
        log.info("User id={} deletes task id={}", currentUser.getId(), taskId);

        Task task = findOwnedTask(taskId, currentUser);
        taskRepository.delete(task);
        log.info("Task id={} deleted", taskId);
    }

    private Task findOwnedTask(Long taskId, User currentUser) {
        return taskRepository.findByIdAndOwnerId(taskId, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Task id={} not found for user id={}", taskId, currentUser.getId());
                    return new ResourceNotFoundException("Task not found: " + taskId);
                });
    }
}