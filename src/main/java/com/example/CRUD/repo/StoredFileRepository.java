package com.example.CRUD.repo;

import com.example.CRUD.Entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findByOrderIdOrderByUploadedAtDesc(Long orderId);
    Optional<StoredFile> findFirstByOrderIdOrderByUploadedAtDesc(Long orderId);
}