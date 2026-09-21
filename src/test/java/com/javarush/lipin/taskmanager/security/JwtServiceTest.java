package com.javarush.lipin.taskmanager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-for-jwt-service-min-32-bytes", 86400000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken("testuser");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("testuser");
        String tampered = token.substring(0, token.length() - 2) + "XX";

        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void shouldRejectGarbage() {
        assertFalse(jwtService.isTokenValid("not.a.token"));
    }
}