package com.facturemanagement.application.ports.output;

import com.facturemanagement.domain.event.FactureCreatedEvent;
import com.facturemanagement.domain.event.FacturePDFGeneratedEvent;

public interface FactureEventPublisher {
    void publishFactureCreatedEvent(FactureCreatedEvent event);
    void publishFactureGeneratePDFEvent(FacturePDFGeneratedEvent event);
}
