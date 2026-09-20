package com.javarush.lipin.taskmanager.controller;

import com.javarush.lipin.taskmanager.dto.TaskCreateRequest;
import com.javarush.lipin.taskmanager.dto.TaskResponse;
import com.javarush.lipin.taskmanager.dto.TaskUpdateRequest;
import com.javarush.lipin.taskmanager.model.entity.TaskStatus;
import com.javarush.lipin.taskmanager.model.entity.User;
import com.javarush.lipin.taskmanager.service.TaskService;
import com.javarush.lipin.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "CRUD-операции с задачами текущего пользователя")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить все мои задачи",
            description = "Возвращает задачи текущего пользователя. Опционально фильтрует по статусу.")
    @ApiResponse(responseCode = "200", description = "Список задач")
    public ResponseEntity<List<TaskResponse>> getAll(
            @Parameter(description = "Фильтр по статусу: NEW, IN_PROGRESS, DONE, CANCELLED")
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(taskService.getAllForUser(currentUser(), status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задачу по id")
    @ApiResponse(responseCode = "200", description = "Задача найдена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена или принадлежит другому пользователю")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getByIdForUser(id, currentUser()));
    }

    @PostMapping
    @Operation(summary = "Создать задачу")
    @ApiResponse(responseCode = "201", description = "Задача создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse created = taskService.create(request, currentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задачу")
    @ApiResponse(responseCode = "200", description = "Задача обновлена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена или принадлежит другому пользователю")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(id, request, currentUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задачу")
    @ApiResponse(responseCode = "204", description = "Задача удалена")
    @ApiResponse(responseCode = "404", description = "Задача не найдена или принадлежит другому пользователю")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id, currentUser());
        return ResponseEntity.noContent().build();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }
}