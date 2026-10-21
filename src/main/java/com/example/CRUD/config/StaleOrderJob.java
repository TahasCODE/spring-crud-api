package com.example.CRUD.config;

import com.example.CRUD.service.OrderPdfService;
import com.example.CRUD.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Component
public class StaleOrderJob {

    private static final Logger log = LoggerFactory.getLogger(StaleOrderJob.class);
    private static final String LOCK_KEY = "job:expire-stale-orders";

    private final OrderService orderService;
    private final OrderPdfService orderPdfService;
    private final StringRedisTemplate redis;
    private final boolean enabled;
    private final Duration expireAfter;
    private final boolean dryRun;

    public StaleOrderJob(OrderService orderService, OrderPdfService orderPdfService, StringRedisTemplate redis,
                         @Value("${app.orders.expire-enabled:true}") boolean enabled,
                         @Value("${app.orders.expire-after:7d}") Duration expireAfter,
                         @Value("${app.orders.expire-dry-run:false}") boolean dryRun) {
        this.orderService = orderService;
        this.orderPdfService = orderPdfService;
        this.redis = redis;
        this.enabled = enabled;
        this.expireAfter = expireAfter;
        this.dryRun = dryRun;
    }

    @Scheduled(cron = "${app.orders.expire-cron:0 0 * * * *}")   // default: every hour
    public void run() {
        if (!enabled) return;
        boolean locked;
        try {   // only one instance may run the job at a time
            locked = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(LOCK_KEY, "running", Duration.ofMinutes(10)));
        } catch (Exception e) {
            log.warn("Stale order job skipped, Redis unavailable: {}", e.getMessage());
            return;
        }
        if (!locked) {
            log.info("Stale order job skipped: another run is in progress");
            return;
        }
        try {
            List<Long> ids = orderService.expireStaleOrders(expireAfter, dryRun);
            if (ids.isEmpty()) {
                log.info("Stale order job: no pending orders older than {}", expireAfter);
                return;
            }
            log.info("{} {} pending order(s) older than {}: {}",
                    dryRun ? "DRY RUN, would cancel" : "Cancelled", ids.size(), expireAfter, ids);
            if (!dryRun) {
                for (Long id : ids) {   // keep the stored PDFs in step with the new status
                    try { orderPdfService.generateAndStore(id); }
                    catch (RuntimeException e) { log.warn("Could not refresh PDF for order {}", id, e); }
                }
            }
        } catch (RuntimeException e) {
            log.error("Stale order job failed", e);
        } finally {
            try { redis.delete(LOCK_KEY); } catch (Exception ignored) { }
        }
    }
}