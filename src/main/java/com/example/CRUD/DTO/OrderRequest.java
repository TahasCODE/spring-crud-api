package com.example.CRUD.DTO;
import com.example.CRUD.Entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record OrderRequest(
        @NotNull(message = "placerId is required") Long placerId,
        Long supplierId,
        @NotNull(message = "totalAmount is required") @PositiveOrZero BigDecimal totalAmount,
        OrderStatus status
) {}