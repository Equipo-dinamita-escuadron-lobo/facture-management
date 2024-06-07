package com.facturemanagement.infraestructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PRODUCTS")
public class ProductEntity {
    @Id
    @Column(name = "product_id", nullable = false, unique = true)
    private long productId;

    @Column(name = "product_amount", nullable = false)
    private double amount;

    @Column(name = "product_description", nullable = false)
    private String description;
    
    @Column(name = "product_vat", nullable = false)
    private double vat;
    
    @Column(name = "product_unit_price", nullable = false)
    private double unitPrice;
    
    @Column(name = "product_subtotal", nullable = false)
    private double subtotal;
}
