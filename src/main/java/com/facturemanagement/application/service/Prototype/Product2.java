package com.facturemanagement.application.service.Prototype;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product2 {
    private long productId;
    private int amount;
    private String description;
    private double descount;
    //private String code;
    //private double vat;
    private double unitPrice; 
    private double subtotal;

    private List<Integer> taxPercentage;



    public double getBasePrice() {
        // Aplica descuento
        double priceAfterDiscount = unitPrice * (1 - descount / 100.0);

        // Suma los porcentajes de impuestos
        int totalTaxPercent = taxPercentage.stream().mapToInt(Integer::intValue).sum();

        // Calcula el valor de impuestos
        double taxAmount = priceAfterDiscount * (totalTaxPercent / 100.0);

        // Resta los impuestos al precio después del descuento
        return priceAfterDiscount - taxAmount;
    }
}
