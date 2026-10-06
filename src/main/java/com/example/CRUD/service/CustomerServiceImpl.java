package com.example.CRUD.service;

import com.example.CRUD.Entity.Customer;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.CustomerRepository;
import com.example.CRUD.repo.OrderPlacerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderPlacerRepository orderPlacerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    @Override
    public Customer create(Customer customer) {
        if (customer.getEmail() != null && orderPlacerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + customer.getEmail());
        }
        customer.setId(null);
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(Long id, Customer updated) {
        Customer existing = getById(id);
        if (updated.getEmail() != null
                && !updated.getEmail().equals(existing.getEmail())
                && orderPlacerRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + updated.getEmail());
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return customerRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        customerRepository.delete(getById(id));
    }
}