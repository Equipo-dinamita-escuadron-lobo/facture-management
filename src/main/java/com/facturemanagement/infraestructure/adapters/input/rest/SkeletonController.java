package com.facturemanagement.infraestructure.adapters.input.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.facturemanagement.application.service.Prototype.Facture2;
import com.facturemanagement.application.service.Prototype.ISkeleton;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/factures/")
@RequiredArgsConstructor
public class SkeletonController {
    private final ISkeleton skeletonService;

    @PostMapping("/skeleton")
    public void createSkeleton(@RequestBody Facture2 facture) {
        skeletonService.skeletonPurchaseKardex(facture);
    }
}
