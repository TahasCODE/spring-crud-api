package com.example.CRUD.controller;

import com.example.CRUD.DTO.CustomerRequest;
import com.example.CRUD.DTO.CustomerResponse;
import com.example.CRUD.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public CustomerResponse getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return customerService.getVisible(id, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return customerService.updateVisible(id, request, Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { customerService.delete(id); }
}