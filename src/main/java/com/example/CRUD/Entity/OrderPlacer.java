package com.example.CRUD.Entity;

import java.math.BigDecimal;



import java.math.BigDecimal;

@FunctionalInterface
public interface OrderPlacer {
    Order placeOrder(BigDecimal totalAmount, OrderStatus status, Supplier supplier);
}