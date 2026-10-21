package com.example.CRUD.DTO;

import com.example.CRUD.Entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSearchCriteria(String type, OrderStatus status, Long placerId, Long supplierId,
                                  BigDecimal minAmount, BigDecimal maxAmount,
                                  LocalDate from, LocalDate to) {}