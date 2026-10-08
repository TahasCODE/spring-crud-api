package com.example.CRUD.controller;

import com.example.CRUD.DTO.*;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.service.OrderPdfService;
import com.example.CRUD.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderPdfService orderPdfService;

    @GetMapping
    public List<OrderResponse> getAll() { return orderService.getAll(); }

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

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.create(request);
        refreshPdf(created.id());          // the PDF is saved in the vault right away
        return created;
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

    private void refreshPdf(Long orderId) {
        try {
            orderPdfService.generateAndStore(orderId);
        } catch (RuntimeException e) {
            // the order is already saved, so don't fail the request; POST /{id}/pdf can retry
            log.warn("Could not store PDF for order {}", orderId, e);
        }
    }
}