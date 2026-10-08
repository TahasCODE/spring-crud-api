package com.example.CRUD.service;

import com.example.CRUD.DTO.EmployeeRequest;
import com.example.CRUD.DTO.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> getAll();
    EmployeeResponse getById(Long id);
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
}