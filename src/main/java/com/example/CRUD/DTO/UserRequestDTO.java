package com.example.CRUD.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {
    @NotNull(message = "Name is required")
    @Size(max = 150, message = "Name can be of most 150 characters")
    private String name;


    @NotNull(message = "Designation is required")
    @Size(max = 200, message = "Email can be of most 200 characters")
    private String designation;

}


