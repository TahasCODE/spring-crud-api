package com.example.CRUD.DTO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
public record DashboardResponse(
        BigDecimal totalSales,
        BigDecimal totalPurchases,
        List<OrderSummary> ordersByStatus,
        List<SupplierTotal> topSuppliers,
        List<DayCount> ordersLast7Days) implements Serializable { }