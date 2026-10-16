package com.example.CRUD.service;

import com.example.CRUD.DTO.*;
import com.example.CRUD.repo.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final OrderRepository orderRepository;

    public DashboardService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Cacheable("dashboard")
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.minusDays(6);
        Map<LocalDate, Long> perDay = orderRepository.orderDatesSince(first.atStartOfDay()).stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        List<DayCount> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = first.plusDays(i);
            days.add(new DayCount(d, perDay.getOrDefault(d, 0L)));
        }
        return new DashboardResponse(
                orderRepository.totalSales(),
                orderRepository.totalPurchases(),
                orderRepository.summarizeByStatus(),
                orderRepository.topSuppliers(PageRequest.of(0, 5)),
                days);
    }
}