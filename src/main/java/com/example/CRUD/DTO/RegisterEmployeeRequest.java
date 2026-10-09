
package com.example.CRUD.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterEmployeeRequest(
        @NotBlank(message = "name is required") @Size(max = 100) String name,
        @NotBlank(message = "email is required") @Email(message = "email is invalid") @Size(max = 100) String email,
        @Size(max = 20) String phone,
        @Size(max = 255) String address,
        @NotBlank(message = "designation is required") @Size(max = 100) String designation,
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be 8 to 72 characters") String password) {}