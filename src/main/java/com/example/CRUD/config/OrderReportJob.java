package com.example.CRUD.config;

import com.example.CRUD.DTO.OrderSummary;
import com.example.CRUD.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReportJob {

    private final OrderService orderService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Scheduled(cron = "${app.report.cron:0 0 18 * * *}", zone = "Asia/Karachi")
    public void logDailySummary() {
        try {
            List<OrderSummary> summary = orderService.getSummary();
            log.info("=== Daily order report ===");
            for (OrderSummary row : summary) {
                log.info("{}: {} orders, total {}", row.status(), row.orderCount(), row.totalAmount());
            }
            log.info("=== End of report ===");
        } catch (Exception e) {
            log.error("Daily order report failed", e);
        }
    }
}