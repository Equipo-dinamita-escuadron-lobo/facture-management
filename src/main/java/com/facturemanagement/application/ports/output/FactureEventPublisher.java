package com.facturemanagement.application.ports.output;

import com.facturemanagement.domain.event.FactureCreatedEvent;

public interface FactureEventPublisher {
    void publishFactureCreatedEvent(FactureCreatedEvent event);
}
