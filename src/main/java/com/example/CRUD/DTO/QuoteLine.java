package com.example.CRUD.DTO;

import java.math.BigDecimal;

public record QuoteLine(String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}