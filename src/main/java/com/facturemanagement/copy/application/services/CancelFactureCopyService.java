package com.facturemanagement.copy.application.services;

import com.facturemanagement.copy.application.input.ICancelFactureCopyPort;
import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import com.facturemanagement.copy.domain.enums.CopyEstado;
import com.facturemanagement.copy.domain.exceptions.DuplicateCopyJobException;
import com.facturemanagement.copy.domain.models.CopyJobLog;
import com.facturemanagement.copy.infraestructure.adapters.input.rest.dto.CopyCancelResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio para cancelar un proceso de copia de facture.
 * ADR-38.
 */
@Service
@RequiredArgsConstructor
public class CancelFactureCopyService implements ICancelFactureCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public CopyCancelResponseDto cancelar(String idProceso) {
        CopyJobLog log = logRepo.buscarPorIdProceso(idProceso)
                .orElseThrow(() -> new DuplicateCopyJobException(idProceso, 0));

        CopyJobLog cancelado = CopyJobLog.builder()
                .idProceso(log.getIdProceso())
                .fase(log.getFase())
                .modulo(log.getModulo())
                .estado(CopyEstado.CANCELADO)
                .fechaInicio(log.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(log.getEquivalenciasGeneradas())
                .build();
        logRepo.guardar(cancelado);

        return CopyCancelResponseDto.builder()
                .estado(CopyEstado.CANCELADO.name())
                .mensaje("Proceso de copia facture cancelado exitosamente")
                .build();
    }
}
