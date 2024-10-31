package com.facturemanagement.application.service;

import com.facturemanagement.application.ports.input.CreateFactureUseCase;
import com.facturemanagement.application.ports.output.FactureCreatedOutputPort;
import com.facturemanagement.domain.event.FactureCreatedEvent;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateFactureService implements CreateFactureUseCase {

    private final FactureCreatedOutputPort factureCreatedOutputPort;

    private final FactureEventPublisherAdapter factureEventPublisher;

    @Override
    public Facture createFacture(Facture facture) {
        // Generar el siguiente factCode
        Long nextFactCode = generateNextFactCode();
        facture.setFactCode(nextFactCode); // Asignar el nuevo factCode
        System.out.println(facture);
        // Guardar la factura
        facture = factureCreatedOutputPort.saveFacture(facture);
        
        // Publicar el evento de creación de factura
        factureEventPublisher.publishFactureCreatedEvent(new FactureCreatedEvent(facture.getFactId()));
        return facture;
    }

    private Long generateNextFactCode() {
        // Obtener el factCode máximo desde el repositorio
        Long lastFactCode = factureCreatedOutputPort.findMaxFactCode();
        return (lastFactCode == null) ? 1L : lastFactCode + 1; // Comienza en 1 si no hay factCode
    }
}
