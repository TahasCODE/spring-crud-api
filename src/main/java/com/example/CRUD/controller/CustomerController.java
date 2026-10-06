package com.example.CRUD.controller;

import com.example.CRUD.DTO.CustomerRequest;
import com.example.CRUD.DTO.CustomerResponse;
import com.example.CRUD.Entity.Customer;
import com.example.CRUD.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public List<CustomerResponse> getAll() { return customerService.getAll(); }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) { return customerService.getById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { customerService.delete(id); }
}