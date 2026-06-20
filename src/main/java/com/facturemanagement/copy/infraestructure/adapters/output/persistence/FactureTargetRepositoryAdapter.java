package com.facturemanagement.copy.infraestructure.adapters.output.persistence;

import com.facturemanagement.copy.application.output.IFactureTargetRepositoryPort;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: escritura de facturas en el tenant de destino.
 * El tenant de destino debe estar activo en TenantContext antes de llamar guardar().
 * ADR-38.
 */
@Component
@RequiredArgsConstructor
public class FactureTargetRepositoryAdapter implements IFactureTargetRepositoryPort {

    private final FactureRepository factureRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public FactureEntity guardar(FactureEntity factura) {
        return factureRepository.save(factura);
    }

    @Override
    @Transactional
    public FactureEntity guardarConProductos(FactureEntity factura) {
        return entityManager.merge(factura);
    }
}
