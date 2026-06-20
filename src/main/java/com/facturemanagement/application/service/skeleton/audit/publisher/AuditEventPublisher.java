package com.facturemanagement.application.service.skeleton.audit.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.facturemanagement.application.service.skeleton.audit.builder.DocumentEventDto;
import com.facturemanagement.infraestructure.adapters.config.rabbitConfig.RabbitAuditPublisherConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @Async
    public void publish(DocumentEventDto dto) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitAuditPublisherConfig.AUDIT_EXCHANGE,
                    RabbitAuditPublisherConfig.DOCUMENT_EVENT_ROUTING_KEY,
                    dto);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria del documento", e);
        }
    }
}
