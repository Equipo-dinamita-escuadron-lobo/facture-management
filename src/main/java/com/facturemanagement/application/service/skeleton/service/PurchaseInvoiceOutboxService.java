package com.facturemanagement.application.service.skeleton.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceStatus;
import com.facturemanagement.application.service.skeleton.model.SkeletonFacture;
import com.facturemanagement.application.service.skeleton.repository.PurchaseInvoiceOutboxRepository;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceOutboxService {
    private final PurchaseInvoiceOutboxRepository repository;
    private final ObjectMapper mapper;
    private final PayableAccountCodeResolver payableAccountCodeResolver;

    public void enqueue(SkeletonFacture facture, String eventType) {
        try {
            String eventId = UUID.randomUUID().toString();
            String tenant = TenantContext.getTenantId();
            String payableCode = payableAccountCodeResolver.resolveOrFail(
                    facture.getAccountingAccount(), facture.getEntId());
            PurchaseInvoiceEventDto body = PurchaseInvoiceEventDto.builder()
                    .invoiceId(facture.getId())
                    .reference(String.valueOf(facture.getFactCode()))
                    .enterpriseId(facture.getEntId())
                    .supplierId(facture.getThId())
                    .originalAmount(new BigDecimal(facture.getTotalValue()))
                    .paidAmount(new BigDecimal(facture.getTotalPay()))
                    .pendingAmount(new BigDecimal(facture.getPendingValue()))
                    .issueDate(facture.getCreatedAt() == null ? LocalDate.now() : facture.getCreatedAt().toLocalDate())
                    .dueDate(facture.getExpirationDate())
                    .payableAccountId(facture.getAccountingAccount())
                    .payableAccountCode(payableCode)
                    .active(facture.getPurchaseStatus() != PurchaseInvoiceStatus.VOIDED)
                    .tenantId(tenant)
                    .build();
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", eventId);
            envelope.put("eventType", eventType);
            envelope.put("eventVersion", 1);
            envelope.put("occurredAt", Instant.now());
            envelope.put("tenantId", tenant);
            envelope.put("enterpriseId", facture.getEntId());
            envelope.put("correlationId", eventId);
            envelope.put("sourceDocument", Map.of("type", "PURCHASE_INVOICE", "id", facture.getId()));
            envelope.put("payload", body);
            PurchaseInvoiceOutboxEvent event = new PurchaseInvoiceOutboxEvent();
            event.setEventId(eventId);
            event.setEventType(eventType);
            event.setPayload(mapper.writeValueAsString(envelope));
            event.setTenantId(tenant);
            repository.save(event);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible crear el outbox de compra", ex);
        }
    }
}
