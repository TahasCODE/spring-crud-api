package com.example.CRUD.service.impl;

import com.example.CRUD.DTO.AuthResponse;
import com.example.CRUD.DTO.LoginRequest;
import com.example.CRUD.DTO.RegisterCustomerRequest;
import com.example.CRUD.DTO.RegisterEmployeeRequest;
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

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

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
        return toAuthResponse(customerRepository.save(c), ROLE_CUSTOMER);
    }

    @Override
    public AuthResponse registerEmployee(RegisterEmployeeRequest r) {
        ensureEmailIsFree(r.email());
        Employee e = new Employee();
        e.setName(r.name());
        e.setEmail(r.email());
        e.setPhone(r.phone());
        e.setAddress(r.address());
        e.setDesignation(r.designation());
        e.setPasswordHash(passwordEncoder.encode(r.password()));
        return toAuthResponse(employeeRepository.save(e), ROLE_EMPLOYEE);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        // same error for an unknown email and a wrong password, so nobody can find out which emails exist
        Person person = personRepository.findByEmail(r.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (person.getPasswordHash() == null
                || !passwordEncoder.matches(r.password(), person.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String role = (person instanceof Employee) ? ROLE_EMPLOYEE : ROLE_CUSTOMER;
        return toAuthResponse(person, role);
    }

    private void ensureEmailIsFree(String email) {
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }
    }

    private AuthResponse toAuthResponse(Person p, String role) {
        return new AuthResponse(
                jwtService.generateToken(p, role),
                "Bearer",
                jwtService.expiresInSeconds(),
                p.getId(), p.getName(), p.getEmail(), role);
    }
}