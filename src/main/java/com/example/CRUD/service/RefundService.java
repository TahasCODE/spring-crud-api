package com.example.CRUD.service;

import com.example.CRUD.DTO.RefundCreateRequest;
import com.example.CRUD.DTO.RefundResponse;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.RefundRequest;
import com.example.CRUD.Entity.RefundStatus;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.OrderRepository;
import com.example.CRUD.repo.RefundRequestRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final OrderRepository orderRepository;
    private final RefundRequestRepository refundRepository;
    private final MeterRegistry meterRegistry;

    @Value("${app.refunds.window-days:14}")
    private int windowDays;

    @Transactional
    public RefundResponse request(Long orderId, Long callerId, RefundCreateRequest body) {
        // lock the order row: a second request for the same order waits here until we commit
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
        if (!"SALE".equals(order.getType()) || !order.getPlacer().getId().equals(callerId)) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Only delivered orders can be refunded");
        }
        LocalDateTime since = order.getDeliveredAt() != null ? order.getDeliveredAt() : order.getOrderDate();
        if (since.plusDays(windowDays).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("The refund window of " + windowDays + " days has passed");
        }
        BigDecimal reserved = refundRepository.sumReserved(orderId, RefundStatus.REJECTED);
        if (reserved == null) {
            reserved = BigDecimal.ZERO;
        }
        BigDecimal left = order.getTotalAmount().subtract(reserved);
        if (body.amount().compareTo(left) > 0) {
            throw new BadRequestException("Refund is more than what is left to refund: " + left);
        }
        RefundRequest saved = refundRepository.save(
                new RefundRequest(orderId, callerId, body.amount(), body.reason()));
        meterRegistry.counter("refunds.requested").increment();
        return toResponse(saved);
    }

    @Transactional
    public RefundResponse decide(Long refundId, Long managerId, boolean approve, String note) {
        refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id " + refundId));
        RefundStatus to = approve ? RefundStatus.APPROVED : RefundStatus.REJECTED;
        // works only while the request is still REQUESTED, even if two managers click together
        int changed = refundRepository.decide(refundId, RefundStatus.REQUESTED, to,
                managerId, LocalDateTime.now(), note);
        if (changed == 0) {
            throw new BadRequestException("This refund request was already decided");
        }
        meterRegistry.counter("refunds.decided", "result", to.name().toLowerCase()).increment();
        return toResponse(refundRepository.findById(refundId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> forOrder(Long orderId, Long callerId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
        boolean allowed = "MANAGER".equals(role)
                || ("CUSTOMER".equals(role) && "SALE".equals(order.getType())
                && order.getPlacer().getId().equals(callerId));
        if (!allowed) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }
        return refundRepository.findByOrderIdOrderByIdAsc(orderId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> list(RefundStatus status) {
        List<RefundRequest> rows = status == null
                ? refundRepository.findTop100ByOrderByIdDesc()
                : refundRepository.findTop100ByStatusOrderByIdDesc(status);
        return rows.stream().map(this::toResponse).toList();
    }

    private RefundResponse toResponse(RefundRequest r) {
        return new RefundResponse(r.getId(), r.getOrderId(), r.getAmount(), r.getReason(),
                r.getStatus(), r.getCreatedAt(), r.getDecidedBy(), r.getDecidedAt(), r.getDecisionNote());
    }
}