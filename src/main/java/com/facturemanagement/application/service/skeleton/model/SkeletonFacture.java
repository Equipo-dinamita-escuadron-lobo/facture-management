package com.facturemanagement.application.service.skeleton.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skeleton_factures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFacture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long factCode;
    
    @Column(nullable = false)
    private String entId;
    
    @Column(nullable = false)
    private Long thId;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SkeletonProduct> products = new HashSet<>();
    
    @Column(nullable = false)
    private String totalValue;
    
    @Column(nullable = false)
    private String totalPay;
    
    @Column(nullable = false)
    private String pendingValue;
    
    private LocalDate expirationDate;
    
    private Long accountingAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkeletonFactureType factureType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Métodos helper para mantener la relación bidireccional
    public void addProduct(SkeletonProduct product) {
        products.add(product);
        product.setFacture(this);
    }
    
    public void removeProduct(SkeletonProduct product) {
        products.remove(product);
        product.setFacture(null);
    }
}
