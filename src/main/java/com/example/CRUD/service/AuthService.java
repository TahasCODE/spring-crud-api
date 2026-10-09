package com.example.CRUD.service;

import com.example.CRUD.DTO.AuthResponse;
import com.example.CRUD.DTO.LoginRequest;
import com.example.CRUD.DTO.RegisterCustomerRequest;
import com.example.CRUD.DTO.RegisterEmployeeRequest;

public interface AuthService {
    AuthResponse registerCustomer(RegisterCustomerRequest request);
    AuthResponse registerEmployee(RegisterEmployeeRequest request);
    AuthResponse login(LoginRequest request);
}