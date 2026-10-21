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

    public Long customerPlacerId() {
        return "CUSTOMER".equals(currentUser.role()) ? currentUser.id() : null;
    }

    public void assertCanViewPlacer(Long placerId) {
        Long customerId = customerPlacerId();
        if (customerId != null && !customerId.equals(placerId)) {
            throw new AccessDeniedException("Customers may only view their own orders");
        }
    }

    public void assertManager() {
        if (!"MANAGER".equals(currentUser.role())) {
            throw new AccessDeniedException("Only managers may modify orders");
        }
    }

    public void assertCanView(Order order) {
        // customers may only see their own orders; cashiers and managers keep their existing access
        if ("CUSTOMER".equals(currentUser.role())
                && !order.getPlacer().getId().equals(currentUser.id())) {
            throw new AccessDeniedException("Customers may only view their own orders");
        }
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
