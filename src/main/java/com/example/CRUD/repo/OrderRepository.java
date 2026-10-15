package com.example.CRUD.repo;

import com.example.CRUD.DTO.OrderSummary;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.PurchaseOrder;
import com.example.CRUD.Entity.SaleOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT new com.example.CRUD.DTO.OrderSummary(o.status, COUNT(o), SUM(o.totalAmount)) "
            + "FROM com.example.CRUD.Entity.Order o GROUP BY o.status")
    List<OrderSummary> summarizeByStatus();

    @Override
    @EntityGraph(attributePaths = "placer")
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = "placer")
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = "placer")
    List<Order> findByPlacerId(Long placerId);

    @EntityGraph(attributePaths = "placer")
    Page<Order> findByOrderDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT o FROM SaleOrder o")
    @EntityGraph(attributePaths = "placer")
    List<SaleOrder> findAllSales();

    @Query("SELECT o FROM PurchaseOrder o")
    @EntityGraph(attributePaths = "placer")
    List<PurchaseOrder> findAllPurchases();

    @Query("SELECT o FROM PurchaseOrder o WHERE o.supplier.id = :supplierId")
    @EntityGraph(attributePaths = "placer")
    List<PurchaseOrder> findPurchasesBySupplier(@Param("supplierId") Long supplierId);
}