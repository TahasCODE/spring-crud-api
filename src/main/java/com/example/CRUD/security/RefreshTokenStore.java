package com.example.CRUD.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
public class RefreshTokenStore {

    private static final String PREFIX = "refresh:";
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenStore(StringRedisTemplate redis,
                             @Value("${app.jwt.refresh-days:7}") long refreshDays) {
        this.redis = redis;
        this.ttl = Duration.ofDays(refreshDays);
    }

    public String create(Long personId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(PREFIX + token, String.valueOf(personId), ttl);
        return token;
    }

    // single use: returns the person id and deletes the token; null if unknown, expired or used
    public Long consume(String token) {
        String key = PREFIX + token;
        String personId = redis.opsForValue().get(key);
        if (personId == null) return null;
        return Boolean.TRUE.equals(redis.delete(key)) ? Long.valueOf(personId) : null;
    }

    public void revoke(String token) { redis.delete(PREFIX + token); }
}