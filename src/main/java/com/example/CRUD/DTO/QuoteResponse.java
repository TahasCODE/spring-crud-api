package com.example.CRUD.DTO;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(List<QuoteLine> lines, BigDecimal subtotal, String discountType,
                            BigDecimal discountPercent, BigDecimal discountAmount,
                            BigDecimal taxableAmount, BigDecimal taxRatePercent,
                            BigDecimal taxAmount, BigDecimal total) {}