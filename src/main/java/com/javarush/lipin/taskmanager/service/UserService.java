package com.javarush.lipin.taskmanager.service;

import com.javarush.lipin.taskmanager.dto.UserResponse;
import com.javarush.lipin.taskmanager.exception.ResourceNotFoundException;
import com.javarush.lipin.taskmanager.exception.UserAlreadyExistsException;
import com.javarush.lipin.taskmanager.model.entity.Role;
import com.javarush.lipin.taskmanager.model.entity.User;
import com.javarush.lipin.taskmanager.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String username, String email, String rawPassword) {
        log.info("Registering new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username '{}' already taken", username);
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email '{}' already taken", email);
            throw new UserAlreadyExistsException("Email already taken: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        log.info("User '{}' registered with id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}