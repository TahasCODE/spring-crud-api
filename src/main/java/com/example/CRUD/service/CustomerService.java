package com.example.CRUD.service;

import com.example.CRUD.DTO.CustomerRequest;
import com.example.CRUD.DTO.CustomerResponse;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getAll();
    CustomerResponse getById(Long id);
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(Long id, CustomerRequest request);
    void delete(Long id);
}