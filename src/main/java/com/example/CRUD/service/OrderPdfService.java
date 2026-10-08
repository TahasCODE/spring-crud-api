package com.example.CRUD.service;

import com.example.CRUD.DTO.FileResponse;

public interface OrderPdfService {
    byte[] getStoredPdf(Long orderId);           // read from the vault only
    FileResponse generateAndStore(Long orderId);  // build, replace the old copy, save in the vault
    void deleteStoredPdfs(Long orderId);
}