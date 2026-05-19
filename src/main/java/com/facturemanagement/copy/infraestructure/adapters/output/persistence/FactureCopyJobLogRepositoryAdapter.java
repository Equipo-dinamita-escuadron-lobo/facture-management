package com.facturemanagement.copy.infraestructure.adapters.output.persistence;

import com.facturemanagement.copy.application.output.ICopyJobLogRepositoryPort;
import com.facturemanagement.copy.domain.enums.CopyEstado;
import com.facturemanagement.copy.domain.models.CopyJobLog;
import com.facturemanagement.copy.infraestructure.adapters.output.persistence.jpa.FactureCopyJobLogEntity;
import com.facturemanagement.copy.infraestructure.adapters.output.persistence.jpa.FactureCopyJobLogJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: persiste el log de idempotencia de copia de facture.
 * Convierte entre modelo de dominio (Instant) y entidad JPA (LocalDateTime) para MySQL.
 * ADR-38.
 */
@Component
@RequiredArgsConstructor
public class FactureCopyJobLogRepositoryAdapter implements ICopyJobLogRepositoryPort {

    private final FactureCopyJobLogJpaRepository jpaRepository;

    @Override
    public CopyJobLog guardar(CopyJobLog log) {
        FactureCopyJobLogEntity entity = toEntity(log);
        FactureCopyJobLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CopyJobLog> buscarPorIdProcesoYFase(String idProceso, int fase) {
        return jpaRepository.findByIdProcesoAndFase(idProceso, fase)
                .map(this::toDomain);
    }

    @Override
    public Optional<CopyJobLog> buscarPorIdProceso(String idProceso) {
        return jpaRepository.findFirstByIdProcesoOrderByFaseDesc(idProceso)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void eliminarPorIdProceso(String idProceso) {
        jpaRepository.deleteByIdProceso(idProceso);
    }

    // ----------------------------------------------------------------
    // Mapper interno domain ↔ entity
    // ----------------------------------------------------------------

    private FactureCopyJobLogEntity toEntity(CopyJobLog log) {
        return FactureCopyJobLogEntity.builder()
                .idProceso(log.getIdProceso() != null ? log.getIdProceso().toString() : null)
                .fase(log.getFase())
                .modulo(log.getModulo())
                .estado(log.getEstado())
                .fechaInicio(toLocalDateTime(log.getFechaInicio()))
                .fechaFin(toLocalDateTime(log.getFechaFin()))
                .equivalenciasGeneradas(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .errorMessage(log.getErrorMessage())
                .build();
    }

    private CopyJobLog toDomain(FactureCopyJobLogEntity entity) {
        return CopyJobLog.builder()
                .idProceso(entity.getIdProceso() != null ? UUID.fromString(entity.getIdProceso()) : null)
                .fase(entity.getFase())
                .modulo(entity.getModulo())
                .estado(entity.getEstado())
                .fechaInicio(toInstant(entity.getFechaInicio()))
                .fechaFin(toInstant(entity.getFechaFin()))
                .equivalenciasGeneradas(entity.getEquivalenciasGeneradas() != null ? entity.getEquivalenciasGeneradas() : 0)
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    private Instant toInstant(LocalDateTime ldt) {
        return ldt != null ? ldt.toInstant(ZoneOffset.UTC) : null;
    }
}
