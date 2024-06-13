package com.facturemanagement.infraestructure.adapters.output.eventpublisher;

import org.springframework.context.ApplicationEventPublisher;

import com.facturemanagement.application.ports.output.FactureEventPublisher;
import com.facturemanagement.domain.event.FactureCreatedEvent;
import com.facturemanagement.domain.event.FacturePDFGeneratedEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FactureEventPublisherAdapter implements FactureEventPublisher{
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishFactureCreatedEvent(FactureCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishFactureGeneratePDFEvent(FacturePDFGeneratedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
