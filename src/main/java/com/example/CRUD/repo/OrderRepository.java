package com.example.CRUD.repo;

import com.example.CRUD.DTO.OrderSummary;
import com.example.CRUD.DTO.SupplierTotal;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.PurchaseOrder;
import com.example.CRUD.Entity.SaleOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {


    @EntityGraph(attributePaths = "placer")
    Page<Order> findByPlacerId(Long placerId, Pageable pageable);

    @Query("SELECT new com.example.CRUD.DTO.OrderSummary(o.status, COUNT(o), SUM(o.totalAmount)) "
            + "FROM com.example.CRUD.Entity.Order o GROUP BY o.status")
    List<OrderSummary> summarizeByStatus();


    @Query("SELECT o.id FROM Order o WHERE o.status = :status AND o.orderDate < :cutoff ORDER BY o.id")
    List<Long> findStaleIds(@Param("status") OrderStatus status, @Param("cutoff") LocalDateTime cutoff);

    // changes the status ONLY if the order still has the expected status; returns 1 if it did, 0 if not
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Order o set o.status = :to, o.version = o.version + 1 "
            + "where o.id = :id and o.status = :from")
    int transitionStatus(@Param("id") Long id,
                         @Param("from") OrderStatus from,
                         @Param("to") OrderStatus to);

    @Override
    @EntityGraph(attributePaths = "placer")
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

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

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SaleOrder s")
    BigDecimal totalSales();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseOrder p")
    BigDecimal totalPurchases();

    @Query("SELECT new com.example.CRUD.DTO.SupplierTotal(p.supplier.name, SUM(p.totalAmount)) "
            + "FROM PurchaseOrder p GROUP BY p.supplier.id, p.supplier.name "
            + "ORDER BY SUM(p.totalAmount) DESC")
    List<SupplierTotal> topSuppliers(Pageable pageable);

    @Query("SELECT o.orderDate FROM Order o WHERE o.orderDate >= :from")
    List<LocalDateTime> orderDatesSince(@Param("from") LocalDateTime from);


}