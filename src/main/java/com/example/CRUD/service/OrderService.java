package com.example.CRUD.service;

import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.OrderType;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getAll(OrderType type); // type == null returns everything
    OrderResponse getById(Long id);
    List<OrderResponse> getByPlacer(Long placerId);
    List<OrderResponse> getBySupplier(Long supplierId);
    OrderResponse create(OrderRequest request);
    OrderResponse updateStatus(Long id, OrderStatus status);
    void delete(Long id);
}