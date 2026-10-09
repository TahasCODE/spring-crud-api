package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.*;
import com.example.CRUD.Entity.Customer;
import com.example.CRUD.Entity.Employee;
import com.example.CRUD.Entity.Person;
import com.example.CRUD.exception.DuplicateResourceException;
import com.example.CRUD.exception.InvalidCredentialsException;
import com.example.CRUD.repo.CustomerRepository;
import com.example.CRUD.repo.EmployeeRepository;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.security.JwtService;
import com.example.CRUD.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final PersonRepository personRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse registerCustomer(RegisterCustomerRequest r) {
        ensureEmailIsFree(r.email());
        Customer c = new Customer();
        c.setName(r.name());
        c.setEmail(r.email());
        c.setPhone(r.phone());
        c.setAddress(r.address());
        c.setPasswordHash(passwordEncoder.encode(r.password()));
        Customer saved = customerRepository.save(c);
        return toAuthResponse(saved);
    }

    @Override
    public EmployeeResponse registerEmployee(RegisterEmployeeRequest r) {
        ensureEmailIsFree(r.email());
        Employee e = new Employee();
        e.setName(r.name());
        e.setEmail(r.email());
        e.setPhone(r.phone());
        e.setAddress(r.address());
        e.setDesignation(r.designation());
        e.setEmployeeType(r.employeeType());
        e.setPasswordHash(passwordEncoder.encode(r.password()));
        Employee s = employeeRepository.save(e);
        return new EmployeeResponse(s.getId(), s.getName(), s.getEmail(), s.getPhone(),
                s.getAddress(), s.getDesignation(), s.getEmployeeType());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        Person person = personRepository.findByEmail(r.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (person.getPasswordHash() == null
                || !passwordEncoder.matches(r.password(), person.getPasswordHash())) {
            throw new InvalidCredentialsException();   // same error either way
        }
        return toAuthResponse(person);
    }

    // CUSTOMER, CASHIER or MANAGER; an employee without a type gets EMPLOYEE, which has no order permissions
    private String roleOf(Person p) {
        if (p instanceof Employee e) {
            return e.getEmployeeType() != null ? e.getEmployeeType().name() : "EMPLOYEE";
        }
        return "CUSTOMER";
    }

    private void ensureEmailIsFree(String email) {
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }
    }

    private AuthResponse toAuthResponse(Person p) {
        String role = roleOf(p);
        return new AuthResponse(jwtService.generateToken(p, role), "Bearer",
                jwtService.expiresInSeconds(), p.getId(), p.getName(), p.getEmail(), role);
    }
}