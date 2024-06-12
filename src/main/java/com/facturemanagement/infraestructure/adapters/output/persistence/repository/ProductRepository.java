package com.facturemanagement.infraestructure.adapters.output.persistence.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>{
    @Query("SELECT p FROM ProductEntity p INNER JOIN p.factures f WHERE f.factId = :factId")
    Set<ProductEntity> getProductsByFactureId(Long factId);
}
