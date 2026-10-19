package com.example.CRUD.DTO;
import java.io.Serializable;
import java.math.BigDecimal;
public record SupplierTotal(String supplierName, BigDecimal totalAmount) implements Serializable { }