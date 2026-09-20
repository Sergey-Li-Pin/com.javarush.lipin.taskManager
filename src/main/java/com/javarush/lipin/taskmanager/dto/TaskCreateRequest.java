package com.javarush.lipin.taskmanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskCreateRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @Future(message = "Deadline must be in the future")
        LocalDateTime deadline
) {
}