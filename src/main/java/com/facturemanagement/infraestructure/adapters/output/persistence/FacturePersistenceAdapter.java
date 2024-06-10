package com.facturemanagement.infraestructure.adapters.output.persistence;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.domain.model.Product;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.ProductPersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FacturePersistenceAdapter implements FactureCreatedOutputPort, FactureGetOutputPort{

    @PersistenceContext
    private EntityManager entityManager;

    private final FactureRepository factureRepository;

    private final FacturePersistenceMapper facturePersistenceMapper;
    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public Facture saveFacture(Facture facture) {

        System.out.println("Entrando a saveFacture");

        if(facture.getFactProducts().isEmpty()){
            return null;
        }

        FactureEntity factureEntity = this.facturePersistenceMapper.toFactureEntity(facture);
        Set<ProductEntity> productEntities = new HashSet<ProductEntity>();

        for(Product product: facture.getFactProducts()){
            productEntities.add(this.productPersistenceMapper.toProductEntity(product));
        }

        factureEntity.setFactProducts(productEntities);

        factureRepository.save(factureEntity);

        return this.facturePersistenceMapper.toFacture(factureEntity);
    }

    @Override
    public Optional<Facture> getFactureById(Long factId) {
        System.out.println("Entrando a getFactureById");
        Optional<FactureEntity> factureEntity =  this.factureRepository.findById(factId);
        if(factureEntity.isEmpty()){
            return Optional.empty();
        }
        Facture facture = this.facturePersistenceMapper.toFacture(factureEntity.get());
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
        return this.facturePersistenceMapper.toFacture(factureEntity);
    }
}
