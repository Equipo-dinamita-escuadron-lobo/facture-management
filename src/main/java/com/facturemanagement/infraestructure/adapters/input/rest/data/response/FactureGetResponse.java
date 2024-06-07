package com.facturemanagement.infraestructure.adapters.input.rest.data.response;

import java.time.LocalDate;
import java.util.Set;

import com.facturemanagement.domain.model.Product;
import com.facturemanagement.domain.model.eFactureType;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FactureGetResponse {
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
