package com.example.CRUD.repo;

import com.example.CRUD.Entity.OrderPlacer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPlacerRepository extends JpaRepository<OrderPlacer, Long> {
    boolean existsByEmail(String email);
}