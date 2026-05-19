package com.facturemanagement.copy.application.input;

import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyPhaseResponseDto;

/**
 * Puerto de entrada: ejecutar una fase de copia del módulo facture.
 * ADR-38.
 */
public interface IExecuteFactureCopyPhasePort {

    CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request);
}
