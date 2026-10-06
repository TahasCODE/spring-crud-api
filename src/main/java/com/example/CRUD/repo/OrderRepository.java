package com.example.CRUD.repo;

import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.OrderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"orderPlacer", "supplier"})
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = {"orderPlacer", "supplier"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"orderPlacer", "supplier"})
    List<Order> findByType(OrderType type);

    @EntityGraph(attributePaths = {"orderPlacer", "supplier"})
    List<Order> findByOrderPlacerId(Long placerId);

    @EntityGraph(attributePaths = {"orderPlacer", "supplier"})
    List<Order> findBySupplierId(Long supplierId);
}