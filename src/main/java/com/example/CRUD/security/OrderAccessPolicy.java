package com.example.CRUD.security;

import com.example.CRUD.DTO.OrderSearchCriteria;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.PurchaseOrder;
import com.example.CRUD.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAccessPolicy {

    private final CurrentUser currentUser;

    public void assertCanView(Order order) {
        String role = currentUser.role();
        boolean sale = !(order instanceof PurchaseOrder);
        boolean allowed = switch (role) {
            case "MANAGER" -> true;
            case "CASHIER" -> sale;
            case "EMPLOYEE" -> !sale;
            case "CUSTOMER" -> sale && order.getPlacer().getId().equals(currentUser.id());
            default -> false;
        };
        if (!allowed) {
            // same answer as a missing order, so ids cannot be probed
            throw new ResourceNotFoundException("Order not found with id " + order.getId());
        }
    }

    // cashiers only search sale orders, employees only purchase orders; managers search everything
    public OrderSearchCriteria scope(OrderSearchCriteria c) {
        String only = switch (currentUser.role()) {
            case "CASHIER" -> "SALE";
            case "EMPLOYEE" -> "PURCHASE";
            case "MANAGER" -> null;
            default -> throw new AccessDeniedException("Role not allowed to search orders");
        };
        if (only == null) {
            return c;
        }
        if (c.type() != null && !c.type().equals(only)) {
            throw new AccessDeniedException("Your role can only search " + only + " orders");
        }
        return new OrderSearchCriteria(only, c.status(), c.placerId(), c.supplierId(),
                c.minAmount(), c.maxAmount(), c.from(), c.to());
    }

    public void assertCanPlace(Order order) {
        String role = currentUser.role();
        Long userId = currentUser.id();
        Long placerId = order.getPlacer().getId();

        boolean allowed;
        if (order instanceof PurchaseOrder) {
            // supplier orders: managers only, and only as themselves
            allowed = "MANAGER".equals(role) && placerId.equals(userId);
        } else {
            // simple orders: cashiers for any customer, customers only for themselves
            allowed = "CASHIER".equals(role)
                    || ("CUSTOMER".equals(role) && placerId.equals(userId));
        }

        if (!allowed) {
            throw new AccessDeniedException("Role " + role + " is not allowed to place this kind of order");
        }

    }
}
