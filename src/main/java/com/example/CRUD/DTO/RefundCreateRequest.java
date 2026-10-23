package com.example.CRUD.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RefundCreateRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 200) String reason) {}