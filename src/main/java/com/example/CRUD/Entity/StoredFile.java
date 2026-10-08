package com.example.CRUD.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stored_files",
        indexes = @Index(name = "idx_stored_files_order", columnList = "order_id"))
@Getter @Setter @NoArgsConstructor
public class StoredFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String displayName;      // the original name, duplicates allowed

    @Column(nullable = false, unique = true, length = 100)
    private String storedName;       // UUID name on disk

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "order_id")
    private Long orderId;            // set only for order PDFs

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}