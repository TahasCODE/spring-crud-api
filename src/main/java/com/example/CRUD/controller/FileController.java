package com.example.CRUD.controller;

import com.example.CRUD.DTO.FileDownload;
import com.example.CRUD.DTO.FileResponse;
import com.example.CRUD.service.FileVaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileVaultService fileVaultService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse upload(@RequestParam("file") MultipartFile file) {
        return fileVaultService.upload(file);
    }

    @GetMapping
    public List<FileResponse> list() {
        return fileVaultService.list();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileDownload d = fileVaultService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(d.fileName(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(d.contentType()))
                .body(d.resource());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        fileVaultService.delete(id);
    }
}