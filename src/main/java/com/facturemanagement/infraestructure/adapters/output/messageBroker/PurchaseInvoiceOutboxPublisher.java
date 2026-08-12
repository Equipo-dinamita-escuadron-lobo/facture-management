package com.facturemanagement.infraestructure.adapters.output.messageBroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import com.facturemanagement.application.service.skeleton.repository.PurchaseInvoiceOutboxRepository;
import com.facturemanagement.infraestructure.adapters.config.rabbitConfig.RabbitReceiptConfig;
import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import com.facturemanagement.infraestructure.adapters.security.FactureServiceTokenProvider;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PurchaseInvoiceOutboxPublisher {
    private final PurchaseInvoiceOutboxRepository repository;private final RabbitTemplate rabbit;private final ObjectMapper mapper;private final FactureServiceTokenProvider tokens;

    public PurchaseInvoiceOutboxPublisher(PurchaseInvoiceOutboxRepository repository,
            @Qualifier("rabbitTemplate") RabbitTemplate rabbit,
            ObjectMapper mapper,
            FactureServiceTokenProvider tokens) {
        this.repository = repository;
        this.rabbit = rabbit;
        this.mapper = mapper;
        this.tokens = tokens;
    }
    @Scheduled(fixedDelayString="${facture.outbox.delay-ms:5000}") @Transactional
    public void publish(){for(var event:repository.findReadyAcrossTenants()){TenantContext.setTenantId(event.getTenantId());try{CorrelationData correlation=new CorrelationData(event.getEventId());rabbit.convertAndSend(RabbitReceiptConfig.PURCHASE_INVOICE_EXCHANGE,"",mapper.readTree(event.getPayload()),message->{message.getMessageProperties().setMessageId(event.getEventId());message.getMessageProperties().setHeader("eventId",event.getEventId());message.getMessageProperties().setHeader("eventType",event.getEventType());message.getMessageProperties().setHeader("tenantId",event.getTenantId());message.getMessageProperties().setHeader("x-tenant-id",event.getTenantId());message.getMessageProperties().setHeader("x-jwt-token",tokens.bearerToken());return message;},correlation);var confirm=correlation.getFuture().get(5,TimeUnit.SECONDS);if(!confirm.isAck())throw new IllegalStateException("Rabbit rechazo evento de compra: "+confirm.getReason());if(correlation.getReturned()!=null)throw new IllegalStateException("Rabbit retorno evento de compra sin ruta");event.setStatus(PurchaseInvoiceOutboxEvent.Status.PUBLISHED);event.setPublishedAt(Instant.now());event.setLastError(null);}catch(Exception ex){int attempts=event.getAttempts()+1;event.setAttempts(attempts);event.setStatus(PurchaseInvoiceOutboxEvent.Status.FAILED);event.setLastError(ex.getMessage());event.setNextAttemptAt(Instant.now().plusSeconds(Math.min(300,1L<<Math.min(attempts,8))));}finally{TenantContext.clear();}}}
}
