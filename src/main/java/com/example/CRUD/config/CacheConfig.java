package com.example.CRUD.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    @Primary
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("suppliers",
                Caffeine.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build());
        return manager;
    }

    @Bean
    CacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues();
        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }
}