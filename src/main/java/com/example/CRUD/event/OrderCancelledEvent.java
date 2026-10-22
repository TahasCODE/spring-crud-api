package com.example.CRUD.event;

public record OrderCancelledEvent(Long orderId, String cancelledBy) {}