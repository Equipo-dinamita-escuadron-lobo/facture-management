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
public class FactureListRequest {
    @NotNull(message = "Enterprise Id cannot ve null.")
    private String entId;
}
