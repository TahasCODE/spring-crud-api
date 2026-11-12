package com.example.CRUD.config;

import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.miniredis.enabled", havingValue = "true")
public class MiniRedisClientConfig {

    @Bean
    LettuceClientOptionsBuilderCustomizer miniRedisResp2() {
        return options -> options.protocolVersion(ProtocolVersion.RESP2);
    }
}