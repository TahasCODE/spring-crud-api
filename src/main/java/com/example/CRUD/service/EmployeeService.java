package com.example.CRUD.service;

import com.example.CRUD.DTO.EmployeeResponse;
import com.example.CRUD.Entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> getAll();
    Employee getById(Long id);
    Employee create(Employee employee);
    Employee update(Long id, Employee employee);
    void delete(Long id);
}