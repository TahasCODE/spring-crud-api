package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.FileDownload;
import com.example.CRUD.DTO.FileResponse;
import com.example.CRUD.Entity.StoredFile;
import com.example.CRUD.config.FileVaultProperties;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.StoredFileRepository;
import com.example.CRUD.service.FileVaultService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileVaultServiceImpl implements FileVaultService {

    private final StoredFileRepository repository;
    private final FileVaultProperties properties;

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(properties.directory()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String originalName = cleanName(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extensionOf(originalName);

        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new BadRequestException("Invalid file name");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);   // no REPLACE_EXISTING, so nothing can be overwritten
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file", e);
        }

        StoredFile sf = new StoredFile();
        sf.setDisplayName(originalName);
        sf.setStoredName(storedName);
        sf.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        sf.setSize(file.getSize());

        try {
            return toResponse(repository.save(sf));
        } catch (RuntimeException e) {
            deleteQuietly(target);   // keep disk and database in sync
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload download(Long id) {
        StoredFile sf = find(id);
        Path path = root.resolve(sf.getStoredName()).normalize();
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File content is missing for id " + id);
        }
        return new FileDownload(new FileSystemResource(path), sf.getDisplayName(), sf.getContentType());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StoredFile sf = find(id);
        repository.delete(sf);
        deleteQuietly(root.resolve(sf.getStoredName()).normalize());
    }

    private StoredFile find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + id));
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("File name is missing");
        }
        String n = StringUtils.cleanPath(name);
        n = n.substring(n.lastIndexOf('/') + 1).trim();   // drop any folder part
        if (n.isBlank() || n.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
        return n;
    }



    private String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 ? name.substring(dot) : "";
        return ext.matches("\\.[A-Za-z0-9]{1,10}") ? ext : "";
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private FileResponse toResponse(StoredFile f) {
        return new FileResponse(f.getId(), f.getDisplayName(), f.getContentType(), f.getSize(), f.getUploadedAt());
    }
}