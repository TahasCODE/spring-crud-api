package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.CustomerRequest;
import com.example.CRUD.DTO.CustomerResponse;
import com.example.CRUD.Entity.Customer;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.CustomerRepository;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.service.CustomerService;
import com.example.CRUD.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PersonRepository personRepository;
    private final ProfilePictureService profilePictureService;

    @Override @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) { return toResponse(find(id)); }

    @Override
    public CustomerResponse create(CustomerRequest r) {
        if (r.email() != null && personRepository.existsByEmail(r.email())) {
            throw new DuplicateResourceException("Email already in use: " + r.email());
        }
        Customer c = new Customer();
        apply(c, r);
        return toResponse(customerRepository.save(c));
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest r) {
        Customer c = find(id);
        if (r.email() != null && !r.email().equals(c.getEmail()) && personRepository.existsByEmail(r.email())) {
            throw new DuplicateResourceException("Email already in use: " + r.email());
        }
        apply(c, r);
        return toResponse(customerRepository.save(c));
    }

    @Override
    public void delete(Long id) {
        Customer c = find(id);
        profilePictureService.deleteIfExists(id);   // no orphan picture left behind
        customerRepository.delete(c);
    }

    private Customer find(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private void apply(Customer c, CustomerRequest r) {
        c.setName(r.name()); c.setEmail(r.email()); c.setPhone(r.phone()); c.setAddress(r.address());
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress());
    }
}