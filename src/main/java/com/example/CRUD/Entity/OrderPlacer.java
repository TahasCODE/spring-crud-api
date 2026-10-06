package com.example.CRUD.Entity;

import java.math.BigDecimal;

public interface OrderPlacer {
    Long getId();
    String getName();
    Order placeOrder(BigDecimal totalAmount, OrderStatus status, Supplier supplier);
}