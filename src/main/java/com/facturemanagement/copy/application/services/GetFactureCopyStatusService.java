package com.facturemanagement.copy.application.services;

import com.facturemanagement.copy.application.input.IGetFactureCopyStatusPort;
import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import com.facturemanagement.copy.domain.exceptions.DuplicateCopyJobException;
import com.facturemanagement.copy.domain.models.CopyJobLog;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyStatusResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio para consultar el estado de un proceso de copia de facture.
 * ADR-38.
 */
@Service
@RequiredArgsConstructor
public class GetFactureCopyStatusService implements IGetFactureCopyStatusPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public CopyStatusResponseDto obtenerEstado(String idProceso) {
        CopyJobLog log = logRepo.buscarPorIdProceso(idProceso)
                .orElseThrow(() -> new DuplicateCopyJobException(idProceso, 0));

        return CopyStatusResponseDto.builder()
                .fase(log.getFase() != null ? log.getFase() : 0)
                .estado(log.getEstado() != null ? log.getEstado().name() : "DESCONOCIDO")
                .registrosProcesados(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .intentos(1)
                .ultimoError(log.getErrorMessage())
                .build();
    }
}
