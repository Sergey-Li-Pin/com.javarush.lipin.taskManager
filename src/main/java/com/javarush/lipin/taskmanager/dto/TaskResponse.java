package com.javarush.lipin.taskmanager.dto;

import com.javarush.lipin.taskmanager.model.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime deadline,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}