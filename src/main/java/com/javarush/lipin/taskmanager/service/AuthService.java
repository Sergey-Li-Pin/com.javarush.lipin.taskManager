package com.javarush.lipin.taskmanager.service;

import com.javarush.lipin.taskmanager.dto.AuthResponse;
import com.javarush.lipin.taskmanager.dto.LoginRequest;
import com.javarush.lipin.taskmanager.dto.RegisterRequest;
import com.javarush.lipin.taskmanager.dto.UserResponse;
import com.javarush.lipin.taskmanager.model.entity.User;
import com.javarush.lipin.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(RegisterRequest request) {
        User user = userService.register(request.username(), request.email(), request.password());
        return userService.toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user '{}'", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtService.generateToken(request.username());
        log.info("User '{}' logged in, token issued", request.username());
        return new AuthResponse(token);
    }
}