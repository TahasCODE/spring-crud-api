package com.example.CRUD.controller;

import com.example.CRUD.DTO.*;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.service.IdempotencyStore;
import com.example.CRUD.service.OrderPdfService;
import com.example.CRUD.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderPdfService orderPdfService;
    private final IdempotencyStore idempotencyStore;

    @GetMapping("/export")
    public void export(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       HttpServletResponse response) throws IOException {
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > 366) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "from must not be after to, and the range must be at most one year");
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"orders_" + from + "_to_" + to + ".csv\"");
        orderService.exportCsv(from, to, response.getWriter());
    }

    @GetMapping("/summary")
    public List<OrderSummary> getSummary() {
        return orderService.getSummary();
    }

    @GetMapping("/date-range")
    public Page<OrderResponse> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getByDateRange(from, to, page, size);
    }

    @GetMapping
    public List<OrderResponse> getAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return orderService.getAll(page, size);
    }

    @GetMapping("/sales")
    public List<OrderResponse> getSales() { return orderService.getSales(); }

    @GetMapping("/purchases")
    public List<OrderResponse> getPurchases() { return orderService.getPurchases(); }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) { return orderService.getById(id); }

    @GetMapping("/placer/{placerId}")
    public List<OrderResponse> getByPlacer(@PathVariable Long placerId) { return orderService.getByPlacer(placerId); }

    @GetMapping("/supplier/{supplierId}")
    public List<OrderResponse> getBySupplier(@PathVariable Long supplierId) { return orderService.getBySupplier(supplierId); }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderRequest request,
            Principal principal) {

        String redisKey;
        Duration ttl;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyKey.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key is too long");
            }
            redisKey = "idem:" + principal.getName() + ":" + idempotencyKey;
            ttl = Duration.ofHours(24);
        } else {
            redisKey = "dup:" + fingerprint(principal.getName(), request);
            ttl = Duration.ofSeconds(60);
        }

        if (!idempotencyStore.claim(redisKey, ttl)) {
            String existing = idempotencyStore.find(redisKey);
            if (existing == null || idempotencyStore.isInProgress(existing)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An identical request is still being processed");
            }
            return ResponseEntity.ok()
                    .header("Idempotent-Replayed", "true")
                    .body(orderService.getById(Long.parseLong(existing)));
        }
        try {
            OrderResponse created = createOrder(request);
            idempotencyStore.complete(redisKey, created.id(), ttl);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            idempotencyStore.release(redisKey);   // failed attempts can be retried
            throw e;
        }
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        OrderResponse updated = orderService.updateStatus(id, status);
        refreshPdf(id);                    // the stored PDF always matches the order
        return updated;
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.delete(id);           // 404 if the order doesn't exist
        orderPdfService.deleteStoredPdfs(id);
    }

    // reads from the vault only: 404 until the PDF has been created
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = orderPdfService.getStoredPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // builds the PDF and saves it in the vault (for orders created before this feature, or a failed save)
    @PostMapping("/{id}/pdf") @ResponseStatus(HttpStatus.CREATED)
    public FileResponse createPdf(@PathVariable Long id) {
        return orderPdfService.generateAndStore(id);
    }

    private OrderResponse createOrder(OrderRequest request) {
        OrderResponse created = orderService.create(request);
        refreshPdf(created.id());          // the PDF is saved in the vault right away
        return created;
    }

    // same details from the same user produce the same fingerprint; any changed field changes it
    private String fingerprint(String user, OrderRequest r) {
        String amount = r.totalAmount() == null ? "" : r.totalAmount().stripTrailingZeros().toPlainString();
        String raw = user + "|" + r.placerId() + "|" + r.supplierId() + "|" + amount + "|" + r.status();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void refreshPdf(Long orderId) {
        try {
            orderPdfService.generateAndStore(orderId);
        } catch (RuntimeException e) {
            log.warn("Could not store PDF for order {}", orderId, e);
        }
    }
}