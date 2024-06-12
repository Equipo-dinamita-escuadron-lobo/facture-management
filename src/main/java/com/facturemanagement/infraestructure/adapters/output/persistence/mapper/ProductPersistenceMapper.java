package com.facturemanagement.infraestructure.adapters.output.persistence.mapper;

import org.mapstruct.Mapper;

import com.facturemanagement.domain.model.Product;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {
    ProductEntity toProductEntity(Product product);
    
    Product toProduct(ProductEntity productEntity);
}
