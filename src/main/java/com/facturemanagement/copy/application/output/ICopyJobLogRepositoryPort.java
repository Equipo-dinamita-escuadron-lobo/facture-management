package com.facturemanagement.copy.application.output;

import com.facturemanagement.copy.domain.models.CopyJobLog;

import java.util.Optional;

/**
 * Puerto de salida: persistencia del log de idempotencia de copia de facture.
 * ADR-38.
 */
public interface ICopyJobLogRepositoryPort {

    CopyJobLog guardar(CopyJobLog log);

    Optional<CopyJobLog> buscarPorIdProcesoYFase(String idProceso, int fase);

    Optional<CopyJobLog> buscarPorIdProceso(String idProceso);

    void eliminarPorIdProceso(String idProceso);
}
