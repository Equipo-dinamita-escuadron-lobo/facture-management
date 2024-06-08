package com.facturemanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private long productId;
    private long thId;
    private double amount;
    private String description;
    private double vat;
    private double unitPrice;
    private double subtotal;
}
