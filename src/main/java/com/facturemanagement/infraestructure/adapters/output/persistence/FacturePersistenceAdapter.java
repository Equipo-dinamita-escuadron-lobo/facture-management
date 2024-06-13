package com.facturemanagement.infraestructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
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

        System.out.println("Entrando a saveFacture");

        if(facture.getFactProducts().isEmpty()){
            return null;
        }

        FactureEntity factureEntity = this.facturePersistenceMapper.toFactureEntity(facture);

        factureRepository.save(factureEntity);

        return this.convertToFacture(factureEntity);
    }

    @Override
    public Optional<Facture> getFactureById(Long factId) {
        System.out.println("Entrando a getFactureById");
        Optional<FactureEntity> factureOptional =  this.factureRepository.findById(factId);
        if(factureOptional.isEmpty()){
            return Optional.empty();
        }

        FactureEntity factureEntity = factureOptional.get();
        factureEntity.setFactProducts(this.productRepository.getProductsByFactureId(factId));

        Facture facture = this.facturePersistenceMapper.toFacture(factureEntity);

        return Optional.of(facture);
    }

    @Override
    public Page<Facture> getAllFacturesBy(String entId, Pageable pageable) {
        System.out.println("Entrando a getAllFacturesBy");
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllByEnterpriseId(entId, pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    @Override
    public Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable) {
        System.out.println("Entrando a getAllSalesFacturesBy");
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllSalesFacturesByEnterpriseId(entId, pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    @Override
    public Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable) {
        System.out.println("Entrando a getAllShoppingFacturesBy");
        Page<FactureEntity> pageFactureEntities = this.factureRepository.findAllShoppingFacturesByEnterpriseId(entId, pageable);
        return pageFactureEntities.map(this::convertToFacture);
    }

    private Facture convertToFacture(FactureEntity factureEntity){
        System.out.println("\n Entrando a convertir en objeto factura\n");
        factureEntity.setFactProducts(this.productRepository.getProductsByFactureId(factureEntity.getFactId()));
        return this.facturePersistenceMapper.toFacture(factureEntity);
    }
}
