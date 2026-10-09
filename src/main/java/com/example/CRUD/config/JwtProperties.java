package com.example.CRUD.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("app.jwt.expiration-minutes must be greater than 0");
        }
    }
}