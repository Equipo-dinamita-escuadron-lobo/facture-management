package com.facturemanagement.copy.application.output;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de salida: lectura de facturas del tenant de origen.
 * ADR-38.
 */
public interface IFactureSourceRepositoryPort {

    List<FactureEntity> findByEntOrigenBeforeSnapshot(String entOrigen, Instant snapshotCorte);
}
