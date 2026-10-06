package com.example.CRUD.service;

import com.example.CRUD.DTO.SupplierResponse;
import com.example.CRUD.Entity.Supplier;

import java.util.List;

public interface SupplierService {
    List<SupplierResponse> getAll();
    Supplier getById(Long id);
    Supplier create(Supplier supplier);
    Supplier update(Long id, Supplier supplier);
    void delete(Long id);
}
