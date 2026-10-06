package com.example.CRUD.controller;

import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.OrderType;
import com.example.CRUD.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getAll(@RequestParam(required = false) OrderType type) {
        return orderService.getAll(type);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/placer/{placerId}")
    public List<OrderResponse> getByPlacer(@PathVariable Long placerId) {
        return orderService.getByPlacer(placerId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<OrderResponse> getBySupplier(@PathVariable Long supplierId) {
        return orderService.getBySupplier(supplierId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}