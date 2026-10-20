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
import com.example.CRUD.security.LoginAttemptService;
import com.example.CRUD.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.CRUD.security.RefreshTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final PersonRepository personRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenStore refreshTokenStore;




    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        Long personId = refreshTokenStore.consume(refreshToken);
        if (personId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid or expired refresh token"));
        return toAuthResponse(person);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenStore.revoke(refreshToken);
    }

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
        loginAttemptService.checkNotBlocked(r.email());

        Person person = personRepository.findByEmail(r.email()).orElse(null);
        if (person == null || person.getPasswordHash() == null
                || !passwordEncoder.matches(r.password(), person.getPasswordHash())) {
            loginAttemptService.recordFailure(r.email());
            throw new InvalidCredentialsException();
        }
        loginAttemptService.reset(r.email());
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
        return new AuthResponse(jwtService.generateToken(p, role), refreshTokenStore.create(p.getId()),
                "Bearer", jwtService.expiresInSeconds(), p.getId(), p.getName(), p.getEmail(), role);
    }
}