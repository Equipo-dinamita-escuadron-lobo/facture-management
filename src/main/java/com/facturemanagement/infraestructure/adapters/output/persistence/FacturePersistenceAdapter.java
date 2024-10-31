package com.facturemanagement.infraestructure.adapters.output.persistence;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.domain.model.Product;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FacturePersistenceAdapter implements FactureCreatedOutputPort, FactureGetOutputPort{

    @PersistenceContext
    private EntityManager entityManager;

    private final FactureRepository factureRepository;
    private final ProductRepository productRepository;

    private final FacturePersistenceMapper facturePersistenceMapper;

    @Override
    public Long findMaxFactCode() {
        return factureRepository.findMaxFactCode(); // Implement your logic to get the max factCode
    }

    @Override
    @Transactional
    public Facture saveFacture(Facture facture) {
        System.out.println("Entrando a saveFacture");

        if (facture.getFactProducts().isEmpty()) {
            return null;
        }

        Set<Product> productsAux = facture.getFactProducts();

        FactureEntity factureEntity = this.facturePersistenceMapper.toFactureEntity(facture);

        Set<Long> productIds = facture.getFactProducts().stream()
                                      .map(Product::getProductId)
                                      .collect(Collectors.toSet());
        Set<ProductEntity> products = productRepository.findAllById(productIds).stream().collect(Collectors.toSet());

        //listar los id de los producros que ya estan en la base de datos
        Set<Long> productIdsExist = products.stream()
                                        .map(ProductEntity::getProductId)
                                        .collect(Collectors.toSet());

        factureEntity.getFactProducts().clear();

        for (Product product : productsAux) {
            if (!productIdsExist.contains(product.getProductId())) {
                products.add(this.facturePersistenceMapper.toProductEntity(product));
            }
        }

        factureEntity.getFactProducts().addAll(products);


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
