package com.example.CRUD.service;

import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.DTO.OrderSearchCriteria;
import com.example.CRUD.DTO.OrderSummary;
import com.example.CRUD.Entity.OrderStatus;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    OrderResponse cancel(Long id, Long callerId, String role);
    List<Long> expireStaleOrders(Duration olderThan, boolean dryRun);
    void exportCsv(LocalDate from, LocalDate to, Writer writer) throws IOException;
    List<OrderSummary> getSummary();
    Page<OrderResponse> search(OrderSearchCriteria criteria, int page, int size, String sortBy, String direction);
    Page<OrderResponse> getByDateRange(LocalDate from, LocalDate to, int page, int size);
    List<OrderResponse> getAll(int page,int size);
    List<OrderResponse> getAll();
    List<OrderResponse> getSales();
    List<OrderResponse> getPurchases();
    OrderResponse getById(Long id);
    List<OrderResponse> getByPlacer(Long placerId);
    List<OrderResponse> getBySupplier(Long supplierId);
    OrderResponse create(OrderRequest request);
    OrderResponse updateStatus(Long id, OrderStatus status);
    void delete(Long id);
}