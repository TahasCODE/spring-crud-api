package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.CustomerResponse;
import com.example.CRUD.DTO.EmployeeResponse;
import com.example.CRUD.Entity.Employee;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.EmployeeRepository;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(),e.getName(),e.getEmail(),e.getPhone(),e.getDesignation(),e.getAddress());
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    @Override
    public Employee create(Employee employee) {
        if (employee.getEmail() != null && personRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + employee.getEmail());
        }
        employee.setId(null);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        if (updated.getEmail() != null
                && !updated.getEmail().equals(existing.getEmail())
                && personRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + updated.getEmail());
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setDesignation(updated.getDesignation());
        return employeeRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        employeeRepository.delete(getById(id));
    }
}