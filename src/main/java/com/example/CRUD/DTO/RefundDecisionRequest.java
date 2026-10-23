package com.example.CRUD.DTO;

import jakarta.validation.constraints.Size;

public record RefundDecisionRequest(@Size(max = 200) String note) {}