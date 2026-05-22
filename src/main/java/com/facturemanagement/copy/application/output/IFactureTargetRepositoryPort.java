package com.facturemanagement.copy.application.output;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

/**
 * Puerto de salida: escritura de facturas en el tenant de destino.
 * ADR-38.
 */
public interface IFactureTargetRepositoryPort {

    FactureEntity guardar(FactureEntity factura);

    /**
     * Persiste la factura usando em.merge() para que el cascade sea MERGE en lugar de PERSIST.
     * Esto permite que productos ya existentes (mismo productId) sean reutilizados sin
     * duplicate key al insertar en la tabla FACTURES_AND_PRODUCTS.
     */
    FactureEntity guardarConProductos(FactureEntity factura);
}
