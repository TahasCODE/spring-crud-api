package com.example.CRUD.DTO;

import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        OrderType type,
        Long placerId,
        String placerName,
        Long supplierId,
        String supplierName,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount
) {}