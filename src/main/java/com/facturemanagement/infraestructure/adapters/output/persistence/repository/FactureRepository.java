package com.facturemanagement.infraestructure.adapters.output.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

@Repository
public interface FactureRepository extends JpaRepository<FactureEntity, Long>{
    
}
