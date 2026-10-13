package com.example.CRUD.security;

import com.example.CRUD.exception.TooManyAttemptsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final Cache<String, Integer> failures;

    public LoginAttemptService(@Value("${app.login.max-attempts:5}") int maxAttempts,
                               @Value("${app.login.block-minutes:10}") long blockMinutes) {
        this.maxAttempts = maxAttempts;
        this.failures = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(blockMinutes))
                .maximumSize(10_000)
                .build();
    }

    public void checkNotBlocked(String email) {
        Integer count = failures.getIfPresent(key(email));
        if (count != null && count >= maxAttempts) {
            throw new TooManyAttemptsException();
        }
    }

    public void recordFailure(String email) {
        failures.asMap().merge(key(email), 1, Integer::sum);
    }

    public void reset(String email) {
        failures.invalidate(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}