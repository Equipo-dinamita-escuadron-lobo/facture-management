package com.facturemanagement.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceStatus;
import com.facturemanagement.application.service.skeleton.model.SkeletonFacture;
import com.facturemanagement.application.service.skeleton.model.SkeletonFactureType;
import com.facturemanagement.application.service.skeleton.repository.PurchaseInvoiceOutboxRepository;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.PurchaseInvoiceEventPublisher;
import com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.util.TenantContext;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PurchaseInvoiceOutboxServiceUnitTest {
    private PurchaseInvoiceOutboxRepository repository;
    private PayableAccountCodeResolver accountCodes;
    private PurchaseInvoiceEventPublisher publisher;
    private PurchaseInvoiceOutboxService service;

    @BeforeEach
    void setUp() {
        repository = mock(PurchaseInvoiceOutboxRepository.class);
        accountCodes = mock(PayableAccountCodeResolver.class);
        publisher = mock(PurchaseInvoiceEventPublisher.class);
        service = new PurchaseInvoiceOutboxService(
                repository, new ObjectMapper().findAndRegisterModules(), accountCodes, publisher);
        TenantContext.setTenantId("tenant-a");
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void publishesFreshPurchaseWithStableIdentifiersBalancesAndActiveFlag() throws Exception {
        when(accountCodes.resolveOrFail(55L, "enterprise-a")).thenReturn("220501");

        service.enqueue(invoice(), "PURCHASE_INVOICE_CREATED");

        ArgumentCaptor<PurchaseInvoiceOutboxEvent> captured =
                ArgumentCaptor.forClass(PurchaseInvoiceOutboxEvent.class);
        verify(publisher).publish(captured.capture());
        PurchaseInvoiceOutboxEvent event = captured.getValue();
        var envelope = new ObjectMapper().findAndRegisterModules().readTree(event.getPayload());
        var payload = envelope.get("payload");
        assertThat(envelope.get("tenantId").asText()).isEqualTo("tenant-a");
        assertThat(envelope.get("enterpriseId").asText()).isEqualTo("enterprise-a");
        assertThat(payload.get("tenantId").asText()).isEqualTo("tenant-a");
        assertThat(payload.get("enterpriseId").asText()).isEqualTo("enterprise-a");
        assertThat(payload.get("invoiceId").asLong()).isEqualTo(10L);
        assertThat(payload.get("supplierId").asLong()).isEqualTo(77L);
        assertThat(payload.get("originalAmount").decimalValue()).isEqualByComparingTo("100000");
        assertThat(payload.get("paidAmount").decimalValue()).isEqualByComparingTo("40000");
        assertThat(payload.get("pendingAmount").decimalValue()).isEqualByComparingTo("60000");
        assertThat(payload.get("active").asBoolean()).isTrue();
        assertThat(payload.get("payableAccountId").asLong()).isEqualTo(55L);
        assertThat(payload.get("payableAccountCode").asText()).isEqualTo("220501");
        assertThat(event.getStatus()).isEqualTo(PurchaseInvoiceOutboxEvent.Status.PUBLISHED);
    }

    @Test
    void propagatesTemporaryPublishFailureSoOwningTransactionCanRollBack() throws Exception {
        when(accountCodes.resolveOrFail(55L, "enterprise-a")).thenReturn("220501");
        doThrow(new IllegalStateException("rabbit unavailable"))
                .when(publisher).publish(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> service.enqueue(invoice(), "PURCHASE_INVOICE_CREATED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("publicar");
    }

    private SkeletonFacture invoice() {
        return SkeletonFacture.builder()
                .id(10L)
                .factCode(90010L)
                .entId("enterprise-a")
                .thId(77L)
                .totalValue("100000")
                .totalPay("40000")
                .pendingValue("60000")
                .expirationDate(LocalDate.of(2026, 9, 16))
                .accountingAccount(55L)
                .factureType(SkeletonFactureType.PURCHASE)
                .purchaseStatus(PurchaseInvoiceStatus.ACTIVE)
                .build();
    }
}
