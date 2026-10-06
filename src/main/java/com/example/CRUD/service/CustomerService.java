package com.example.CRUD.service;

import com.example.CRUD.Entity.Customer;

import java.util.List;

public interface CustomerService {
    List<Customer> getAll();
    Customer getById(Long id);
    Customer create(Customer customer);
    Customer update(Long id, Customer customer);
    void delete(Long id);
}