package com.facturemanagement.domain.service;

import com.facturemanagement.application.ports.input.CreateFactureUseCase;
import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.domain.event.FactureCreatedEvent;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateFactureService implements CreateFactureUseCase{
    
    private final FactureCreatedOutputPort factureCreatedOutputPort;
    private final FactureEventPublisherAdapter factureEventPublisher;
    @Override
    public Facture createFacture(Facture facture) {
        facture = factureCreatedOutputPort.saveFacture(facture);
        factureEventPublisher.publishFactureCreatedEvent(new FactureCreatedEvent(facture.getFactId()));
        return facture;
    }    
}
