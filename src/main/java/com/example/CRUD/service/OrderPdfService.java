package com.example.CRUD.service;

public interface OrderPdfService {
    byte[] generateOrderPdf(Long orderId);
}