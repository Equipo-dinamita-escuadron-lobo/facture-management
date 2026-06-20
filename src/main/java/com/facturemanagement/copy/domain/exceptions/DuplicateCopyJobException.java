package com.facturemanagement.copy.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un proceso de copia de facture
 * o cuando se intenta operar sobre un proceso inexistente.
 * ADR-38.
 */
public class DuplicateCopyJobException extends RuntimeException {

    public DuplicateCopyJobException(String idProceso, int fase) {
        super(String.format("No se encontró proceso de copia facture con idProceso=%s fase=%d",
                idProceso, fase));
    }
}
