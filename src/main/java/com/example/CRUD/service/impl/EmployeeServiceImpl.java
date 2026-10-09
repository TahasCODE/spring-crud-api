package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.EmployeeRequest;
import com.example.CRUD.DTO.EmployeeResponse;
import com.example.CRUD.Entity.Employee;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.EmployeeRepository;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.service.EmployeeService;
import com.example.CRUD.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;
    private final ProfilePictureService profilePictureService;

    @Override @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) { return toResponse(find(id)); }

    @Override
    public EmployeeResponse create(EmployeeRequest r) {
        if (r.email() != null && personRepository.existsByEmail(r.email())) {
            throw new DuplicateResourceException("Email already in use: " + r.email());
        }
        Employee e = new Employee();
        apply(e, r);
        return toResponse(employeeRepository.save(e));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest r) {
        Employee e = find(id);
        if (r.email() != null && !r.email().equals(e.getEmail()) && personRepository.existsByEmail(r.email())) {
            throw new DuplicateResourceException("Email already in use: " + r.email());
        }
        apply(e, r);
        return toResponse(employeeRepository.save(e));
    }

    @Override
    public void delete(Long id) {
        Employee e = find(id);
        profilePictureService.deleteIfExists(id);   // no orphan picture left behind
        employeeRepository.delete(e);
    }

    private Employee find(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }

    private void apply(Employee e, EmployeeRequest r) {
        e.setName(r.name()); e.setEmail(r.email()); e.setPhone(r.phone());
        e.setAddress(r.address()); e.setDesignation(r.designation());
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail(), e.getPhone(),
                e.getAddress(), e.getDesignation(), e.getEmployeeType());
    }
}