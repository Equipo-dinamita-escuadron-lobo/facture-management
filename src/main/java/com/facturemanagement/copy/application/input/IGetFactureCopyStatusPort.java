package com.facturemanagement.copy.application.input;

import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyStatusResponseDto;

/**
 * Puerto de entrada: consultar el estado de un proceso de copia de facture.
 * ADR-38.
 */
public interface IGetFactureCopyStatusPort {

    CopyStatusResponseDto obtenerEstado(String idProceso);
}
