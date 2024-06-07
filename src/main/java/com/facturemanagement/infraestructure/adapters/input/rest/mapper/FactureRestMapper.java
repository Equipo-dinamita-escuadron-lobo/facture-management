package com.facturemanagement.infraestructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.input.rest.data.request.FactureCreateRequest;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureCreateResponse;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureGetResponse;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureListResponse;

@Mapper
public interface FactureRestMapper {
    Facture toFacture(FactureCreateRequest factureCreateRequest);

    FactureCreateResponse toFactureCreateResponse(int code, String status);

    FactureListResponse toFactureListResponse(Page<Facture> results);

    FactureGetResponse toGetFactureResponse(Facture facture);
}
