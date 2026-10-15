package com.example.CRUD.repo;
import com.example.CRUD.Entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmail(String email);
    List<Supplier> findByNameContainingIgnoreCase(String name);
}