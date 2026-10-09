package com.example.CRUD.repo;


import com.example.CRUD.Entity.Employee;
import com.example.CRUD.Entity.EmployeeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeType(EmployeeType employeeType);
}