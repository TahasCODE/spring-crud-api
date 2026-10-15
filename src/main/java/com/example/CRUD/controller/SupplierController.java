package com.example.CRUD.controller;

import com.example.CRUD.DTO.SupplierRequest;
import com.example.CRUD.DTO.SupplierResponse;
import com.example.CRUD.Entity.Supplier;
import com.example.CRUD.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;


    @GetMapping("/search")
    public List<SupplierResponse> search(@RequestParam String name) {
        return supplierService.search(name);
    }

    @GetMapping
    public List<SupplierResponse> getAll() {
        return supplierService.getAll();
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) {
        return supplierService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Supplier create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(toEntity(request));
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, toEntity(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }

    private Supplier toEntity(SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setContactPerson(request.contactPerson());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        return supplier;
    }
}