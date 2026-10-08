package com.example.CRUD.service;

import com.example.CRUD.DTO.FileDownload;
import com.example.CRUD.DTO.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface FileVaultService {
    FileResponse upload(MultipartFile file);
    FileResponse store(byte[] content, String fileName, String contentType, Long orderId);
    List<FileResponse> list();
    List<FileResponse> listByOrder(Long orderId);
    Optional<byte[]> readLatestByOrder(Long orderId);
    FileDownload download(Long id);
    void delete(Long id);
    void deleteByOrder(Long orderId);
}