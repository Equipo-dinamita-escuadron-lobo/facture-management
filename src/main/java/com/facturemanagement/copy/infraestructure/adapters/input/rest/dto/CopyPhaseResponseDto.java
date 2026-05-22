package com.facturemanagement.copy.infraestructure.adapters.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de response para la ejecución de una fase de copia de facture.
 * Contrato uniforme ADR-38.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseResponseDto {

    private String estado;
    private int registrosProcesados;
    private List<CopyEquivalenciaDto> equivalenciasGeneradas;
    private String mensaje;
    private List<String> advertencias;

    /**
     * Datos serializados de las facturas para la fase RESTORE.
     * Solo presente en modo BACKUP.
     */
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Object datosExportados;
}
