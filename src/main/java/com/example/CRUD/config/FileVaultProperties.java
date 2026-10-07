package com.example.CRUD.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-vault")
public record FileVaultProperties(String directory) {}