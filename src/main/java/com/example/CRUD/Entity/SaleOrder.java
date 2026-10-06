package com.example.CRUD.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SALE")
@NoArgsConstructor
public class SaleOrder extends Order {
    @Override
    public String getType() {
        return "SALE";
    }
}