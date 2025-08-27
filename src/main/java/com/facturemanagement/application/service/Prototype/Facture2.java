package com.facturemanagement.application.service.Prototype;

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
public class Facture2 {
    private Long factId;
    private String entId;
    private Long thId;
    private Long factCode;
    //private String factObservations;
    private FactureType2 factureType;
    private Set<Product2> factProducts;
    //private Double descounts; 
    //private Double factSubtotals;
    //private Double facSalesTax;
    //private Double facWithholdingSource;
    //private LocalDate creationDate;
    //private LocalDate updateDate;

    private String totalValue;
    private String totalPay;
    private String pendingValue;
    private LocalDate expirationDate;

}
