package com.example.CRUD.service;

import com.example.CRUD.DTO.ProfilePictureDownload;
import com.example.CRUD.DTO.ProfilePictureResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {
    ProfilePictureResponse save(Long personId, MultipartFile file);
    ProfilePictureDownload get(Long personId);
    void delete(Long personId);
    void deleteIfExists(Long personId);
}