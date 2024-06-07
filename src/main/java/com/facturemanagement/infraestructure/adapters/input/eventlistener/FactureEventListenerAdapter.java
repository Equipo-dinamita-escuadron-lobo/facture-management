package com.facturemanagement.infraestructure.adapters.input.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.facturemanagement.domain.event.FactureCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FactureEventListenerAdapter {
    
    @EventListener
    public void handleCreate(FactureCreatedEvent event){
        log.info("Facture created with id "+event.getFactId()+" at "+event.getDate());
    }
}
