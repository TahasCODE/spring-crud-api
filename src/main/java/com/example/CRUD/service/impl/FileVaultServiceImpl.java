package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.FileDownload;
import com.example.CRUD.DTO.FileResponse;
import com.example.CRUD.Entity.StoredFile;
import com.example.CRUD.config.FileVaultProperties;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.StoredFileRepository;
import com.example.CRUD.service.FileVaultService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
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
    @Transactional(readOnly = true)
    public FileDownload downloadLatestByOrder(Long orderId) {
        StoredFile sf = repository.findFirstByOrderIdOrderByUploadedAtDesc(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No file stored for order " + orderId));
        Path path = resolvePath(sf.getStoredName());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File content is missing for order " + orderId);
        }
        return new FileDownload(new FileSystemResource(path), sf.getDisplayName(), sf.getContentType());
    }

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String originalName = cleanName(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            return persist(in, originalName, file.getContentType(), file.getSize(), null);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read uploaded file", e);
        }
    }

    @Override
    @Transactional
    public FileResponse store(byte[] content, String fileName, String contentType, Long orderId) {
        if (content == null || content.length == 0) {
            throw new BadRequestException("File is empty");
        }
        String originalName = cleanName(fileName);
        try (InputStream in = new ByteArrayInputStream(content)) {
            return persist(in, originalName, contentType, content.length, orderId);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> listByOrder(Long orderId) {
        return repository.findByOrderIdOrderByUploadedAtDesc(orderId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public Optional<byte[]> readLatestByOrder(Long orderId) {
        Optional<StoredFile> latest = repository.findFirstByOrderIdOrderByUploadedAtDesc(orderId);
        if (latest.isEmpty()) {
            return Optional.empty();
        }
        StoredFile sf = latest.get();
        Path path = resolvePath(sf.getStoredName());
        try {
            if (Files.exists(path)) {
                return Optional.of(Files.readAllBytes(path));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file " + sf.getId(), e);
        }
        repository.delete(sf);   // row points to a missing file, so drop it
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload download(Long id) {
        StoredFile sf = find(id);
        Path path = resolvePath(sf.getStoredName());
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
        deleteQuietly(resolvePath(sf.getStoredName()));
    }

    @Override
    @Transactional
    public void deleteByOrder(Long orderId) {
        for (StoredFile sf : repository.findByOrderIdOrderByUploadedAtDesc(orderId)) {
            repository.delete(sf);
            deleteQuietly(resolvePath(sf.getStoredName()));
        }
    }

    // one place that writes the bytes to disk and the row to the database
    private FileResponse persist(InputStream in, String originalName, String contentType, long size, Long orderId) {
        String storedName = UUID.randomUUID() + extensionOf(originalName);
        Path target = resolvePath(storedName);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(in, target);   // no REPLACE_EXISTING, so nothing can be overwritten
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file", e);
        }

        StoredFile sf = new StoredFile();
        sf.setDisplayName(originalName);
        sf.setStoredName(storedName);
        sf.setContentType(contentType != null ? contentType : "application/octet-stream");
        sf.setSize(size);
        sf.setOrderId(orderId);

        try {
            return toResponse(repository.save(sf));
        } catch (RuntimeException e) {
            deleteQuietly(target);   // keep disk and database in sync
            throw e;
        }
    }

    // 3f2a9c1e-....pdf -> <root>/3f/2a/3f2a9c1e-....pdf (the UUID prefix is the shard)
    private Path resolvePath(String storedName) {
        Path sharded = root.resolve(storedName.substring(0, 2))
                .resolve(storedName.substring(2, 4))
                .resolve(storedName)
                .normalize();
        if (!sharded.startsWith(root)) {
            throw new BadRequestException("Invalid file name");
        }
        if (!Files.exists(sharded)) {
            Path flat = root.resolve(storedName).normalize();   // files saved before sharding existed
            if (flat.startsWith(root) && Files.exists(flat)) {
                return flat;
            }
        }
        return sharded;
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
        return new FileResponse(f.getId(), f.getDisplayName(), f.getContentType(),
                f.getSize(), f.getUploadedAt(), f.getOrderId());
    }
}