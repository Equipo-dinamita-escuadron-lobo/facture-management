package com.facturemanagement.copy.application.output;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

/**
 * Puerto de salida: escritura de facturas en el tenant de destino.
 * ADR-38.
 */
public interface IFactureTargetRepositoryPort {

    FactureEntity guardar(FactureEntity factura);
}
