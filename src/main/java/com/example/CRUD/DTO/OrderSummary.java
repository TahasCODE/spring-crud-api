package com.example.CRUD.DTO;

import com.example.CRUD.Entity.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderSummary(OrderStatus status, long orderCount, BigDecimal totalAmount) implements Serializable { }