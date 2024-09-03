package com.facturemanagement.application.service;

import com.facturemanagement.application.ports.input.GetFactureUseCase;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.exception.FactureNotFound;
import com.facturemanagement.domain.model.Facture;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetFactureService implements GetFactureUseCase{
    private final FactureGetOutputPort factureGetOutputPort;

    @Override
    public Facture getFactureBy(Long factId) {
        return factureGetOutputPort.getFactureById(factId).orElseThrow(()->new FactureNotFound("Facture not found with id "+factId));
    }
    
}
