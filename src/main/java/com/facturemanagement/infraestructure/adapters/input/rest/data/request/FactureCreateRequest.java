package com.facturemanagement.infraestructure.adapters.input.rest.data.request;

import java.time.LocalDate;
import java.util.List;

import com.facturemanagement.domain.model.Product;
import com.facturemanagement.domain.model.eFactureType;

import jakarta.validation.constraints.NotNull;
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
public class FactureCreateRequest {
    private Long factId;
    @NotNull(message = "The holder ID cannot be null.")
    private Long thId;
    
    @NotNull(message = "The invoice code cannot be null.")
    private String factCode;
    
    @NotNull(message = "The invoice type cannot be null.")
    private eFactureType factureType;
    
    @NotNull(message = "The list of products cannot be null.")
    private List<Product> factProducts;
    
    @NotNull(message = "The invoice subtotal cannot be null.")
    private Double factSubtotals;
    
    @NotNull(message = "The sales tax cannot be null.")
    private Double facSalesTax;
    
    @NotNull(message = "The withholding tax cannot be null.")
    private Double facWithholdingSource;

    private LocalDate creationDate;
    private LocalDate updateDate;
}
