package com.facturemanagement.copy.infraestructure.adapters.output.persistence;

import com.facturemanagement.copy.application.output.IFactureSourceRepositoryPort;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Adaptador de salida: lectura de facturas del tenant de origen para la copia.
 * Lee todas las facturas del tenant de origen anteriores al snapshotCorte.
 *
 * NOTA: FactureEntity no tiene campo creationDate con índice temporal fino —
 * se retornan todas las facturas del tenant origen usando JPQL existente
 * y se filtra in-memory por creationDate <= snapshotCorte.
 *
 * ADR-38.
 */
@Component
@RequiredArgsConstructor
public class FactureSourceRepositoryAdapter implements IFactureSourceRepositoryPort {

    private final FactureRepository factureRepository;

    @Override
    public List<FactureEntity> findByEntOrigenBeforeSnapshot(String entOrigen, Instant snapshotCorte) {
        return factureRepository
                .findAllByEntIdForBackup(entOrigen)
                .stream()
                .filter(f -> f.getCreationDate() == null
                        || !f.getCreationDate().toInstant(java.time.ZoneOffset.UTC).isAfter(snapshotCorte))
                .toList();
    }
}
