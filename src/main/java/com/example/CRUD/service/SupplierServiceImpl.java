package com.example.CRUD.service;

import com.example.CRUD.Entity.Supplier;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));
    }

    @Override
    public Supplier create(Supplier supplier) {
        if (supplier.getEmail() != null && supplierRepository.existsByEmail(supplier.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + supplier.getEmail());
        }
        supplier.setId(null);
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier update(Long id, Supplier updated) {
        Supplier existing = getById(id);
        if (updated.getEmail() != null
                && !updated.getEmail().equals(existing.getEmail())
                && supplierRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + updated.getEmail());
        }
        existing.setName(updated.getName());
        existing.setContactPerson(updated.getContactPerson());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return supplierRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        supplierRepository.delete(getById(id));
    }
}