package com.example.CRUD.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
@Getter @Setter @NoArgsConstructor
public class RefundRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long orderId;
    @Column(nullable = false) private Long requestedBy;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 200) private String reason;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private Long decidedBy;
    private LocalDateTime decidedAt;
    @Column(length = 200) private String decisionNote;

    public RefundRequest(Long orderId, Long requestedBy, BigDecimal amount, String reason) {
        this.orderId = orderId;
        this.requestedBy = requestedBy;
        this.amount = amount;
        this.reason = reason;
    }
}