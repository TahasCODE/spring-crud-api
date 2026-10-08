
package com.example.CRUD.DTO;

import java.time.LocalDateTime;

public record ProfilePictureResponse(Long personId, String fileName, String contentType,
                                     long size, LocalDateTime uploadedAt) {}