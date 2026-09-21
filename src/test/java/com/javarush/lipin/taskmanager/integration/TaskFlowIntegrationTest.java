package com.javarush.lipin.taskmanager.integration;

import com.javarush.lipin.taskmanager.dto.AuthResponse;
import com.javarush.lipin.taskmanager.dto.LoginRequest;
import com.javarush.lipin.taskmanager.dto.RegisterRequest;
import com.javarush.lipin.taskmanager.dto.TaskCreateRequest;
import com.javarush.lipin.taskmanager.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullFlow_registerLoginCreateListTask() {
        // 1. Регистрация
        RegisterRequest register = new RegisterRequest("flowuser", "flow@test.com", "secret123");
        ResponseEntity<Void> registerResponse = restTemplate.postForEntity(
                "/api/auth/register", register, Void.class);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        // 2. Логин -> токен
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest("flowuser", "secret123"), AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody().token();
        assertNotNull(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. Создание задачи
        TaskCreateRequest create = new TaskCreateRequest(
                "Integration task", "created from test", LocalDateTime.now().plusDays(1));
        ResponseEntity<TaskResponse> createResponse = restTemplate.exchange(
                "/api/tasks", HttpMethod.POST, new HttpEntity<>(create, headers), TaskResponse.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals("Integration task", createResponse.getBody().title());

        // 4. Список задач содержит созданную
        ResponseEntity<TaskResponse[]> listResponse = restTemplate.exchange(
                "/api/tasks", HttpMethod.GET, new HttpEntity<>(headers), TaskResponse[].class);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().length);
    }

    @Test
    void tasks_withoutToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_withWrongPassword_returns401() {
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest("victim", "victim@test.com", "secret123"), Void.class);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login",
                new LoginRequest("victim", "wrong-password"), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void register_withDuplicateUsername_returns409() {
        RegisterRequest request = new RegisterRequest("dupe", "dupe@test.com", "secret123");
        restTemplate.postForEntity("/api/auth/register", request, Void.class);

        ResponseEntity<String> second = restTemplate.postForEntity("/api/auth/register", request, String.class);

        assertEquals(HttpStatus.CONFLICT, second.getStatusCode());
    }

    @Test
    void user_cannotSeeTasksOfAnotherUser() {
        // user1 создаёт задачу
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest("user1", "u1@test.com", "secret123"), Void.class);
        String token1 = login("user1");
        HttpHeaders headers1 = authHeaders(token1);
        restTemplate.exchange("/api/tasks", HttpMethod.POST,
                new HttpEntity<>(new TaskCreateRequest("private task", null,
                        LocalDateTime.now().plusDays(1)), headers1), TaskResponse.class);

        // user2 не видит задачу user1: 404 (а не 403 — не раскрываем существование)
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequest("user2", "u2@test.com", "secret123"), Void.class);
        HttpHeaders headers2 = authHeaders(login("user2"));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/1", HttpMethod.GET, new HttpEntity<>(headers2), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private String login(String username) {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(username, "secret123"), AuthResponse.class);
        return response.getBody().token();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}