package com.facturemanagement.infraestructure.adapters.output.persistence.mapper;

import org.mapstruct.Mapper;

import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.persistence.entity.FactureEntity;

@Mapper(componentModel = "spring")
public interface FacturePersistenceMapper {
    FactureEntity toFactureEntity(Facture facture);

    Facture toFacture(FactureEntity factureEntity);
}
