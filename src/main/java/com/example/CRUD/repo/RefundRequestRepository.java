package com.example.CRUD.repo;

import com.example.CRUD.Entity.RefundRequest;
import com.example.CRUD.Entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    @Query("select sum(r.amount) from RefundRequest r "
            + "where r.orderId = :orderId and r.status <> :excluded")
    BigDecimal sumReserved(@Param("orderId") Long orderId, @Param("excluded") RefundStatus excluded);

    List<RefundRequest> findByOrderIdOrderByIdAsc(Long orderId);
    List<RefundRequest> findTop100ByStatusOrderByIdDesc(RefundStatus status);
    List<RefundRequest> findTop100ByOrderByIdDesc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RefundRequest r set r.status = :to, r.decidedBy = :by, "
            + "r.decidedAt = :at, r.decisionNote = :note where r.id = :id and r.status = :from")
    int decide(@Param("id") Long id, @Param("from") RefundStatus from, @Param("to") RefundStatus to,
               @Param("by") Long by, @Param("at") LocalDateTime at, @Param("note") String note);
}