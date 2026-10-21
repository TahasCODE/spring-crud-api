package com.example.CRUD.controller;

import com.example.CRUD.DTO.QuoteRequest;
import com.example.CRUD.DTO.QuoteResponse;
import com.example.CRUD.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final PricingService pricingService;

    @PostMapping
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return pricingService.quote(request);
    }
}