package com.example.CRUD.Entity;

import com.example.CRUD.Entity.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PURCHASE")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrder extends Order {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Override
    public String getType() {
        return "PURCHASE";
    }
}