package com.example.CRUD.service;

import com.example.CRUD.DTO.ProfilePictureDownload;
import com.example.CRUD.DTO.ProfilePictureResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {
    ProfilePictureResponse save(Long personId, MultipartFile file, Long callerId, String role);
    ProfilePictureDownload get(Long personId, Long callerId, String role);
    void delete(Long personId, Long callerId, String role);
    void deleteIfExists(Long personId);
}