package com.example.CRUD.event;

import com.example.CRUD.service.ActivityFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.LocalDateTime;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final ActivityFeed feed;

    public OrderEventListener(ActivityFeed feed) {
        this.feed = feed;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCreated(OrderCreatedEvent e) {
        record("ORDER_CREATED | order " + e.orderId() + " | placer " + e.placerId()
                + " | amount " + e.totalAmount().toPlainString());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCancelled(OrderCancelledEvent e) {
        record("ORDER_CANCELLED | order " + e.orderId() + " | by " + e.cancelledBy());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void notifyCustomer(OrderCancelledEvent e) throws InterruptedException {
        log.info("Sending cancellation notice for order {} ...", e.orderId());
        Thread.sleep(3000);   // pretend this is a slow email service
        log.info("Notice sent for order {} (thread {})", e.orderId(), Thread.currentThread().getName());
    }

    private void record(String text) {
        try {
            feed.add(LocalDateTime.now() + " | " + text);
        } catch (Exception ex) {   // a side effect must never break the main action
            log.warn("Could not write activity feed: {}", ex.getMessage());
        }
    }
}