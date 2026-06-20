package com.facturemanagement.copy.application.input;

/**
 * Puerto de entrada: limpiar registros de log de un proceso de copia de facture.
 * ADR-38.
 */
public interface ICleanupFactureCopyPort {

    void limpiar(String idProceso);
}
