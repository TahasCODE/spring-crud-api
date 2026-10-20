package com.example.CRUD.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redis;

    public boolean claim(String key) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, IN_PROGRESS, TTL));
    }
    public String find(String key) { return redis.opsForValue().get(key); }
    public boolean isInProgress(String value) { return IN_PROGRESS.equals(value); }
    public void complete(String key, Long orderId) {
        redis.opsForValue().set(key, String.valueOf(orderId), TTL);
    }
    public void release(String key) { redis.delete(key); }
}