package com.example.CRUD.DTO;

import com.example.CRUD.Entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String type,
        Long placerId,
        String placerName,
        Long supplierId,
        String supplierName,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount
) {}