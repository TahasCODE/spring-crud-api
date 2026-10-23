package com.example.CRUD.DTO;

import com.example.CRUD.Entity.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(Long id, Long orderId, BigDecimal amount, String reason,
                             RefundStatus status, LocalDateTime createdAt, Long decidedBy,
                             LocalDateTime decidedAt, String decisionNote) {}