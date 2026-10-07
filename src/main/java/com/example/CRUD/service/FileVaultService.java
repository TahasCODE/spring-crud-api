package com.example.CRUD.service;

import com.example.CRUD.DTO.FileDownload;
import com.example.CRUD.DTO.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileVaultService {
    FileResponse upload(MultipartFile file);
    List<FileResponse> list();
    FileDownload download(Long id);
    void delete(Long id);
}