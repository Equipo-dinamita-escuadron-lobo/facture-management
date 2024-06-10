package com.facturemanagement.infraestructure.adapters.output.persistence.mapper;

import com.facturemanagement.domain.model.Product;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;

public interface ProductPersistenceMapper {
    ProductEntity toProductEntity(Product product);

    Product toProduct(ProductEntity productEntity);
}
