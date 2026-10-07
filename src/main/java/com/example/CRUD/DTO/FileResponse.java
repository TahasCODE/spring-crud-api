package com.example.CRUD.DTO;



import java.time.LocalDateTime;

public record FileResponse(Long id, String fileName, String contentType, long size, LocalDateTime uploadedAt) {}