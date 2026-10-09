package com.example.CRUD.DTO;

import com.example.CRUD.Entity.EmployeeType;

public record EmployeeResponse(Long id, String name, String email, String phone, String address,
                               String designation, EmployeeType employeeType) {}