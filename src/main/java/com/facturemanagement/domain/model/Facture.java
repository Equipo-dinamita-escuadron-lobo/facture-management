package com.facturemanagement.domain.model;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Facture {
    private Long factId;
    private String entId;
    private Long thId;
    private String factCode;
    @Enumerated(EnumType.STRING)
    private eFactureType factureType;
    private Set<Product> factProducts;
    private Double factSubtotals;
    private Double facSalesTax;
    private Double facWithholdingSource;
    private LocalDate creationDate;
    private LocalDate updateDate;
}
