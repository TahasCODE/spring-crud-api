package com.example.CRUD.service;

import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.DTO.OrderSummary;
import com.example.CRUD.Entity.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    List<OrderSummary> getSummary();
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