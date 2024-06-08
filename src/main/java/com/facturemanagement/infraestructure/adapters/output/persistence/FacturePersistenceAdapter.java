package com.facturemanagement.infraestructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FacturePersistenceAdapter implements FactureCreatedOutputPort, FactureGetOutputPort{

    @PersistenceContext
    private EntityManager entityManager;

    private final FactureRepository factureRepository;
    private final ProductRepository productRepository;

    private final FacturePersistenceMapper facturePersistenceMapper;

    @Override
    public Facture saveFacture(Facture facture) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveFacture'");
    }

    @Override
    public Optional<Facture> getFactureById(Long factId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFactureById'");
    }

    @Override
    public Page<Facture> getAllFacturesBy(String entId, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllFacturesBy'");
    }

    @Override
    public Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllSalesFacturesBy'");
    }

    @Override
    public Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllShoppingFacturesBy'");
    }
    
}
