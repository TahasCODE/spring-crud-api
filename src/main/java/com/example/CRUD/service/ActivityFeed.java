package com.example.CRUD.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ActivityFeed {

    private static final String KEY = "activity:recent";
    private static final int MAX = 50;
    private final StringRedisTemplate redis;

    public ActivityFeed(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void add(String message) {
        redis.opsForList().leftPush(KEY, message);   // newest at the front
        redis.opsForList().trim(KEY, 0, MAX - 1);    // keep only the last 50
    }

    public List<String> recent(int limit) {
        List<String> items = redis.opsForList().range(KEY, 0, Math.min(limit, MAX) - 1);
        return items == null ? List.of() : items;
    }
}