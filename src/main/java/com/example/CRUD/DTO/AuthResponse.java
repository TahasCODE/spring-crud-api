package com.example.CRUD.DTO;

public record AuthResponse(String token, String refreshToken, String tokenType, long expiresInSeconds,
                           Long personId, String name, String email, String role) {}