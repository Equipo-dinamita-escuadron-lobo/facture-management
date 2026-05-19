package com.facturemanagement.copy.application.services;

import com.facturemanagement.copy.application.input.ICleanupFactureCopyPort;
import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para limpiar los registros de log de un proceso de copia de facture.
 * ADR-38.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupFactureCopyService implements ICleanupFactureCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public void limpiar(String idProceso) {
        log.info("Limpiando registros de copia facture para proceso {}", idProceso);
        logRepo.eliminarPorIdProceso(idProceso);
    }
}
