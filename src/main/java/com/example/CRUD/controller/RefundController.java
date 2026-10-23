package com.example.CRUD.controller;

import com.example.CRUD.DTO.RefundCreateRequest;
import com.example.CRUD.DTO.RefundDecisionRequest;
import com.example.CRUD.DTO.RefundResponse;
import com.example.CRUD.Entity.RefundStatus;
import com.example.CRUD.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/api/orders/{orderId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse request(@PathVariable Long orderId,
                                  @Valid @RequestBody RefundCreateRequest body,
                                  @AuthenticationPrincipal Jwt jwt) {
        return refundService.request(orderId, Long.valueOf(jwt.getSubject()), body);
    }

    @GetMapping("/api/orders/{orderId}/refunds")
    public List<RefundResponse> forOrder(@PathVariable Long orderId, @AuthenticationPrincipal Jwt jwt) {
        return refundService.forOrder(orderId, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @GetMapping("/api/refunds")
    public List<RefundResponse> list(@RequestParam(required = false) RefundStatus status) {
        return refundService.list(status);
    }

    @PostMapping("/api/refunds/{id}/approve")
    public RefundResponse approve(@PathVariable Long id,
                                  @Valid @RequestBody(required = false) RefundDecisionRequest body,
                                  @AuthenticationPrincipal Jwt jwt) {
        return refundService.decide(id, Long.valueOf(jwt.getSubject()), true, body == null ? null : body.note());
    }

    @PostMapping("/api/refunds/{id}/reject")
    public RefundResponse reject(@PathVariable Long id,
                                 @Valid @RequestBody(required = false) RefundDecisionRequest body,
                                 @AuthenticationPrincipal Jwt jwt) {
        return refundService.decide(id, Long.valueOf(jwt.getSubject()), false, body == null ? null : body.note());
    }
}