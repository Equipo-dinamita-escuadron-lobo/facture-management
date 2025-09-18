package com.facturemanagement.infraestructure.adapters.input.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.facturemanagement.application.service.Prototype.Facture2;
import com.facturemanagement.application.service.Prototype.ISkeleton;
import com.facturemanagement.application.service.Prototype.Product2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/factures/")
@RequiredArgsConstructor
@Slf4j
public class SkeletonController {
    private final ISkeleton skeletonService;

    @PostMapping("/skeleton/purchase")
    public void createPurchaseSkeleton(@RequestBody Facture2 facture) {
        skeletonService.skeletonPurchaseKardex(facture);
    }

    @PostMapping("/skeleton/sale-for-receipt")
    public void createSaleForReceiptSkeleton(@RequestBody Facture2 facture) {
        boolean receiptSuccess = false;
        boolean kardexSuccess = false;
        
        try {
            skeletonService.skeletonSaleReceipt(facture);
            receiptSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonSaleReceipt: {}", e.getMessage());
        }
        
        try {
            skeletonService.skeletonSaleKardex(facture);
            kardexSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonSaleKardex: {}", e.getMessage());
        }
        
        // Si ninguno de los dos servicios funcionó, lanzar excepción
        if (!receiptSuccess && !kardexSuccess) {
            throw new RuntimeException("Ambos servicios fallaron: skeletonSaleReceipt y skeletonSaleKardex");
        }
    }

    @PostMapping("/skeleton/return-on-sale/{factCode}")
    public void createReturnOnSaleSkeleton(
        @PathVariable Long factCode,
        @RequestBody Product2 product) {
        skeletonService.skeletonReturnOnSaleKardex(factCode, product);
    }

    @PostMapping("/skeleton/return-on-purchase/{factCode}")
    public void createReturnOnPurchaseSkeleton(
        @PathVariable Long factCode,
        @RequestBody Product2 product) {
        skeletonService.skeletonReturnOnPurchaseKardex(factCode, product);
    }

}
