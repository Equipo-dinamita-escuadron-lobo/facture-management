package com.facturemanagement.copy.infraestructure.adapters.output.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para el log de idempotencia de copia de facture.
 * ADR-38.
 */
@Repository
public interface FactureCopyJobLogJpaRepository extends JpaRepository<FactureCopyJobLogEntity, Long> {

    Optional<FactureCopyJobLogEntity> findByIdProcesoAndFase(String idProceso, int fase);

    Optional<FactureCopyJobLogEntity> findFirstByIdProcesoOrderByFaseDesc(String idProceso);

    void deleteByIdProceso(String idProceso);
}
