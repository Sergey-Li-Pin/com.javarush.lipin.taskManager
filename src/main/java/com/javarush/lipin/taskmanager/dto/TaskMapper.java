package com.javarush.lipin.taskmanager.dto;

import com.javarush.lipin.taskmanager.model.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public void updateEntity(Task task, TaskUpdateRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());
        task.setStatus(request.status());
    }
}