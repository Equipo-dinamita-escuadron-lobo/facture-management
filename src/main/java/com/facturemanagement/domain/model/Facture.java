package com.facturemanagement.domain.model;

import java.time.LocalDate;
import java.util.Set;

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
    private Long thId;
    private String factCode;
    private eFactureType factureType;
    private Set<Product> factProducts;
    private Double factSubtotals;
    private Double facSalesTax;
    private Double facWithholdingSource;
    private LocalDate creationDate;
    private LocalDate updateDate;
}
