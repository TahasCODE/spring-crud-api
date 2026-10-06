package com.example.CRUD.Entity;

import com.example.CRUD.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends Person implements OrderPlacer {

    @Column(nullable = false, length = 100)
    private String designation;

    @Override
    public Order placeOrder(BigDecimal totalAmount, OrderStatus status, Supplier supplier) {
        if (supplier == null) {
            throw new BadRequestException("supplierId is required when an employee places an order");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.setPlacer(this);
        order.setSupplier(supplier);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        return order;
    }
}