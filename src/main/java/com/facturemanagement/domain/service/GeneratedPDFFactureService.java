package com.facturemanagement.domain.service;

import com.facturemanagement.application.ports.input.GenerateFacturePDFUseCase;
import com.facturemanagement.application.ports.output.FactureGeneratePDFOutputPort;
import com.facturemanagement.domain.event.FacturePDFGeneratedEvent;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GeneratedPDFFactureService implements GenerateFacturePDFUseCase{

    private final FactureGeneratePDFOutputPort factureGeneratePDFOutputPort;
    private final FactureEventPublisherAdapter factureEventPublisher; 

    @Override
    public byte[] generetePDFFacture(Facture facture) {
        byte[] result = factureGeneratePDFOutputPort.generatePDF(facture);
        factureEventPublisher.publishFactureGeneratePDFEvent(new FacturePDFGeneratedEvent(facture.getFactId()));
        return result;
    }
    
}
