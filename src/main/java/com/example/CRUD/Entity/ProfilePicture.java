package com.example.CRUD.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_pictures")
@Getter @Setter @NoArgsConstructor
public class ProfilePicture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id", nullable = false, unique = true)
    private Long personId;               // a customer or an employee (both are persons)

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Lob                                 // PostgreSQL: oid column, bytes in pg_largeobject
    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}