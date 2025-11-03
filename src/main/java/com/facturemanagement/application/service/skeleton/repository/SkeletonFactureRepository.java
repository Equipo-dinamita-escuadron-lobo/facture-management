package com.facturemanagement.application.service.skeleton.repository;

import com.facturemanagement.application.service.skeleton.model.SkeletonFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkeletonFactureRepository extends JpaRepository<SkeletonFacture, Long> {
    
    Optional<SkeletonFacture> findByFactCode(Long factCode);
    
    boolean existsByFactCode(Long factCode);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.id = :id")
    Optional<SkeletonFacture> findByIdWithProducts(Long id);
    
    @Query("SELECT f FROM SkeletonFacture f LEFT JOIN FETCH f.products WHERE f.factCode = :factCode")
    Optional<SkeletonFacture> findByFactCodeWithProducts(Long factCode);
}
