package com.example.CRUD.security;

import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class CancellationPolicy {

    public static final Duration CUSTOMER_WINDOW = Duration.ofMinutes(15);

    public void assertCanCancel(Order order, Long callerId, String role, LocalDateTime now) {
        OrderStatus status = order.getStatus();

        if ("MANAGER".equals(role)) {          // managers: any order that has not shipped yet
            if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
                throw new BadRequestException("A " + status + " order cannot be cancelled");
            }
            return;
        }
        if ("CUSTOMER".equals(role)) {
            // 1) who: only their own sale orders
            if (!"SALE".equals(order.getType()) || !order.getPlacer().getId().equals(callerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own orders");
            }
            // 2) what state: only pending
            if (status != OrderStatus.PENDING) {
                throw new BadRequestException("Only pending orders can be cancelled by customers; this one is " + status);
            }
            // 3) when: only inside the window
            if (order.getOrderDate().plus(CUSTOMER_WINDOW).isBefore(now)) {
                throw new BadRequestException("The " + CUSTOMER_WINDOW.toMinutes()
                        + "-minute cancellation window has passed; please contact the store");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot cancel orders");
    }
}