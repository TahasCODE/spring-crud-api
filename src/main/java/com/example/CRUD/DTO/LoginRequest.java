
package com.example.CRUD.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email is required") @Email(message = "Email must be a valid email address") String email,
        @NotBlank(message = "password is required") String password) {}