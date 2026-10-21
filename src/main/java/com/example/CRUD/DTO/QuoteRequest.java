package com.example.CRUD.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record QuoteRequest(@NotEmpty @Size(max = 100) @Valid List<QuoteItem> items, String couponCode) {}