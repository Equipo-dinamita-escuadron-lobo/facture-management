package com.facturemanagement.copy.application.input;

import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyCancelResponseDto;

/**
 * Puerto de entrada: cancelar un proceso de copia de facture.
 * ADR-38.
 */
public interface ICancelFactureCopyPort {

    CopyCancelResponseDto cancelar(String idProceso);
}
