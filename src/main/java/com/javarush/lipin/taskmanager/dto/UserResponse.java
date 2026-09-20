package com.javarush.lipin.taskmanager.dto;

import com.javarush.lipin.taskmanager.model.entity.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}