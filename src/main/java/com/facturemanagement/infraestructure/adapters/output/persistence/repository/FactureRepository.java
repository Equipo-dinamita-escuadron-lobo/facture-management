package com.facturemanagement.infraestructure.adapters.output.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

@Repository
public interface FactureRepository extends JpaRepository<FactureEntity, Long> {
    
    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId")
    Page<FactureEntity> findAllByEnterpriseId(String entId, Pageable pageable);

    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId AND f.factureType LIKE 'Venta'")
    Page<FactureEntity> findAllSalesFacturesByEnterpriseId(String entId, Pageable pageable);

    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId AND f.factureType LIKE 'Compra'")
    Page<FactureEntity> findAllShoppingFacturesByEnterpriseId(String entId, Pageable pageable);
}

