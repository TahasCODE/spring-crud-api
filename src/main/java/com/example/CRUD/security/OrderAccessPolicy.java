package com.example.CRUD.security;

import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.PurchaseOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAccessPolicy {

    private final CurrentUser currentUser;

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