package com.facturemanagement.infraestructure.adapters.input.rest.data.request;

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
public class FactureGetRequest {
    @NotNull(message = "Facture Id cannot be null.")
    private long factId;
}
