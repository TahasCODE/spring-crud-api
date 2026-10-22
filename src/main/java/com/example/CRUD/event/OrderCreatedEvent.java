package com.example.CRUD.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, Long placerId, BigDecimal totalAmount) {}