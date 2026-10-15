package com.example.CRUD.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        String contactPerson,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @Pattern(regexp = "^[0-9+\\- ]{7,20}$",
                message = "phone must be 7 to 20 characters: digits, +, - or spaces")
        String phone,

        String address
) { }