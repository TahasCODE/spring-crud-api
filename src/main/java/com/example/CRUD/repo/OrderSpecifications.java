package com.example.CRUD.repo;

import com.example.CRUD.DTO.OrderSearchCriteria;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.PurchaseOrder;
import com.example.CRUD.Entity.SaleOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<Order> matching(OrderSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (c.status() != null) {
                p.add(cb.equal(root.get("status"), c.status()));
            }
            if ("SALE".equals(c.type())) {
                p.add(cb.equal(root.type(), SaleOrder.class));
            }
            if ("PURCHASE".equals(c.type())) {
                p.add(cb.equal(root.type(), PurchaseOrder.class));
            }
            if (c.placerId() != null) {
                p.add(cb.equal(root.get("placer").get("id"), c.placerId()));
            }
            if (c.supplierId() != null) {   // only purchase orders have a supplier
                p.add(cb.equal(cb.treat(root, PurchaseOrder.class).get("supplier").get("id"), c.supplierId()));
            }
            if (c.minAmount() != null) {
                p.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("totalAmount"), c.minAmount()));
            }
            if (c.maxAmount() != null) {
                p.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("totalAmount"), c.maxAmount()));
            }
            if (c.from() != null) {
                p.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("orderDate"), c.from().atStartOfDay()));
            }
            if (c.to() != null) {
                p.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("orderDate"), c.to().atTime(LocalTime.MAX)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}