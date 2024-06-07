package com.facturemanagement.infraestructure.adapters.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>{
    
}
