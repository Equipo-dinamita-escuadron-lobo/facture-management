package com.facturemanagement.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para ejecutar una fase de copia de facture.
 * Contrato uniforme ADR-38.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseRequestDto {

    @NotNull
    private UUID idProceso;

    @Positive
    private int fase;

    @NotBlank
    private String entOrigen;

    @NotBlank
    private String entDestino;

    @NotNull
    private Instant snapshotCorte;

    /**
     * Equivalencias generadas por participantes anteriores.
     * Facture usa tabla "product" para remapear productIds en M:M.
     * ADR-38.
     */
    private List<CopyEquivalenciaDto> equivalenciasPrev;
}
