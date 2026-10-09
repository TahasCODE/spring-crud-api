package com.example.CRUD.service;

import com.example.CRUD.DTO.*;

public interface AuthService {
    AuthResponse registerCustomer(RegisterCustomerRequest request);
    EmployeeResponse registerEmployee(RegisterEmployeeRequest request);   // manager-only, returns no token
    AuthResponse login(LoginRequest request);
}