package com.example.CRUD.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record QuoteItem(@NotBlank String name,
                        @Min(1) @Max(100000) int quantity,
                        @NotNull @DecimalMin("0.01") @Digits(integer = 9, fraction = 2) BigDecimal unitPrice) {}