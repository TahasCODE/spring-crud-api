package com.example.CRUD.config;



import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pdf")
public record PdfProperties(String companyName, String footer, String password) {}