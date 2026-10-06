package com.example.CRUD.service;

import com.example.CRUD.Entity.Supplier;

import java.util.List;

public interface SupplierService {
    List<Supplier> getAll();
    Supplier getById(Long id);
    Supplier create(Supplier supplier);
    Supplier update(Long id, Supplier supplier);
    void delete(Long id);
}
