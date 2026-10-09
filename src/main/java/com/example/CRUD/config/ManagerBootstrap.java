package com.example.CRUD.config;

import com.example.CRUD.Entity.Employee;
import com.example.CRUD.Entity.EmployeeType;
import com.example.CRUD.repo.EmployeeRepository;
import com.example.CRUD.repo.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManagerBootstrap implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.manager.name:Admin Manager}")
    private String name;
    @Value("${app.bootstrap.manager.email:}")
    private String email;
    @Value("${app.bootstrap.manager.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (employeeRepository.existsByEmployeeType(EmployeeType.MANAGER)) {
            return;   // a manager already exists
        }
        if (personRepository.existsByEmail(email)) {
            log.warn("Bootstrap manager not created: email {} is already in use", email);
            return;
        }
        Employee manager = new Employee();
        manager.setName(name);
        manager.setEmail(email);
        manager.setDesignation("Manager");
        manager.setEmployeeType(EmployeeType.MANAGER);
        manager.setPasswordHash(passwordEncoder.encode(password));
        employeeRepository.save(manager);
        log.info("Bootstrap manager created: {}", email);
    }
}