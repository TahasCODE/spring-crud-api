package com.example.CRUD.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private final StringRedisTemplate redis;

    public boolean claim(String key, Duration ttl) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, IN_PROGRESS, ttl));
    }
    public String find(String key) { return redis.opsForValue().get(key); }
    public boolean isInProgress(String value) { return IN_PROGRESS.equals(value); }
    public void complete(String key, Long orderId, Duration ttl) {
        redis.opsForValue().set(key, String.valueOf(orderId), ttl);
    }
    public void release(String key) { redis.delete(key); }
}