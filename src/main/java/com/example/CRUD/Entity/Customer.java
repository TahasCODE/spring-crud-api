package com.example.CRUD.Entity;
import com.example.CRUD.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends Person implements OrderPlacer {

    @Override
    public Order placeOrder(BigDecimal totalAmount, OrderStatus status, Supplier supplier) {
        if (supplier != null) {
            throw new BadRequestException("A customer cannot place an order with a supplier");
        }
        SaleOrder order = new SaleOrder();
        order.setPlacer(this);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        return order;
    }
}