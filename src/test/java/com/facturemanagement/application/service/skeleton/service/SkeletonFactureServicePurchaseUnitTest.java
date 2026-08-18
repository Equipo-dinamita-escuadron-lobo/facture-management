package com.facturemanagement.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturemanagement.application.service.skeleton.dto.SkeletonFactureDetailDto;
import com.facturemanagement.application.service.skeleton.dto.SkeletonFactureRequestDto;
import com.facturemanagement.application.service.skeleton.mapper.SkeletonFactureMapper;
import com.facturemanagement.application.service.skeleton.model.PurchaseInvoiceStatus;
import com.facturemanagement.application.service.skeleton.model.SkeletonFacture;
import com.facturemanagement.application.service.skeleton.model.SkeletonFactureType;
import com.facturemanagement.application.service.skeleton.repository.SkeletonFactureRepository;
import com.facturemanagement.application.service.skeleton.repository.SkeletonReturnRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkeletonFactureServicePurchaseUnitTest {

    private SkeletonFactureRepository factureRepository;
    private SkeletonFactureMapper mapper;
    private PurchaseInvoiceOutboxService outbox;
    private PayableAccountDefaultResolver payableAccountDefaultResolver;
    private SkeletonFactureService service;

    @BeforeEach
    void setUp() {
        factureRepository = mock(SkeletonFactureRepository.class);
        mapper = mock(SkeletonFactureMapper.class);
        outbox = mock(PurchaseInvoiceOutboxService.class);
        payableAccountDefaultResolver = mock(PayableAccountDefaultResolver.class);
        service = new SkeletonFactureService(
                factureRepository,
                mock(SkeletonReturnRepository.class),
                mapper,
                outbox,
                payableAccountDefaultResolver);
    }

    @Test
    void createPurchasePersistsAndEnqueuesCreatedEvent() {
        SkeletonFactureRequestDto request = request(1001L);
        SkeletonFacture invoice = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFactureDetailDto response = SkeletonFactureDetailDto.builder().id(1L).build();
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        when(mapper.toDetailDto(invoice)).thenReturn(response);

        service.createPurchase(request);

        verify(factureRepository).save(invoice);
        verify(outbox).enqueue(invoice, "PURCHASE_INVOICE_CREATED");
    }

    @Test
    void acceptsFullyPendingPurchase() {
        SkeletonFactureRequestDto request = request(1101L, "100000", "0", "100000");

        prepareSuccessfulCreate(request, invoice(11L, PurchaseInvoiceStatus.ACTIVE));

        service.createPurchase(request);

        verify(outbox).enqueue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("PURCHASE_INVOICE_CREATED"));
    }

    @Test
    void acceptsPurchaseWithInitialPartialPayment() {
        SkeletonFactureRequestDto request = request(1102L, "100000.00", "40000.0", "60000");

        prepareSuccessfulCreate(request, invoice(12L, PurchaseInvoiceStatus.ACTIVE));

        service.createPurchase(request);

        verify(factureRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptsFullyPaidPurchase() {
        SkeletonFactureRequestDto request = request(1103L, "100000", "100000", "0.00");

        prepareSuccessfulCreate(request, invoice(13L, PurchaseInvoiceStatus.ACTIVE));

        service.createPurchase(request);

        verify(factureRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsInconsistentPurchaseBalanceBeforePersistence() {
        SkeletonFactureRequestDto request = request(1104L, "100000", "40000", "70000");

        assertThatThrownBy(() -> service.createPurchase(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pendiente");

        verify(factureRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outbox, never()).enqueue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsNegativePurchaseAmounts() {
        SkeletonFactureRequestDto request = request(1105L, "100000", "-1", "100001");

        assertThatThrownBy(() -> service.createPurchase(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativos");
    }

    @Test
    void updatePurchaseEnqueuesUpdatedEvent() {
        SkeletonFacture current = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFacture replacement = invoice(null, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFactureRequestDto request = request(1002L);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(factureRepository.findByIdWithProducts(1L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(request)).thenReturn(replacement);
        when(factureRepository.save(current)).thenReturn(current);

        service.updatePurchase(1L, request);

        verify(outbox).enqueue(current, "PURCHASE_INVOICE_UPDATED");
    }

    @Test
    void voidPurchaseIsIdempotentAndEnqueuesOnlyFirstTransition() {
        SkeletonFacture invoice = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        when(factureRepository.findByIdWithProducts(1L)).thenReturn(Optional.of(invoice));

        service.voidPurchase(1L);
        service.voidPurchase(1L);

        verify(factureRepository).save(invoice);
        verify(outbox).enqueue(invoice, "PURCHASE_INVOICE_VOIDED");
    }

    @Test
    void replayEmitsCurrentStateForEveryPurchase() {
        SkeletonFacture active = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFacture voided = invoice(2L, PurchaseInvoiceStatus.VOIDED);
        when(factureRepository.findByEntIdAndFactureType("enterprise-a", SkeletonFactureType.PURCHASE))
                .thenReturn(List.of(active, voided));

        service.replayPurchases("enterprise-a");

        verify(outbox).enqueue(active, "PURCHASE_INVOICE_CREATED");
        verify(outbox).enqueue(voided, "PURCHASE_INVOICE_VOIDED");
    }

    @Test
    void outboxFailureIsPropagatedSoTheTransactionCanRollBack() {
        SkeletonFactureRequestDto request = request(1003L);
        SkeletonFacture invoice = invoice(3L, PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        doThrow(new IllegalStateException("outbox unavailable"))
                .when(outbox).enqueue(invoice, "PURCHASE_INVOICE_CREATED");

        assertThatThrownBy(() -> service.createPurchase(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outbox unavailable");

        verify(mapper, never()).toDetailDto(invoice);
    }

    private SkeletonFactureRequestDto request(long code) {
        return request(code, "100", "0", "100");
    }

    private SkeletonFactureRequestDto request(long code, String total, String paid, String pending) {
        return SkeletonFactureRequestDto.builder()
                .factCode(code)
                .entId("enterprise-a")
                .factureType(SkeletonFactureType.PURCHASE)
                .products(Set.of())
                .totalValue(total)
                .totalPay(paid)
                .pendingValue(pending)
                .build();
    }

    private void prepareSuccessfulCreate(SkeletonFactureRequestDto request, SkeletonFacture invoice) {
        SkeletonFactureDetailDto response = SkeletonFactureDetailDto.builder().id(invoice.getId()).build();
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        when(mapper.toDetailDto(invoice)).thenReturn(response);
    }

    private SkeletonFacture invoice(Long id, PurchaseInvoiceStatus status) {
        return SkeletonFacture.builder()
                .id(id)
                .factCode(1001L)
                .entId("enterprise-a")
                .thId(77L)
                .products(new java.util.HashSet<>())
                .totalValue("100")
                .totalPay("0")
                .pendingValue("100")
                .factureType(SkeletonFactureType.PURCHASE)
                .purchaseStatus(status)
                .build();
    }
}
