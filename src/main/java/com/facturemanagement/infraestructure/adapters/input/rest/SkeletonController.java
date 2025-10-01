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

        boolean kardexSuccess = false;
        boolean pepsSuccess = false;

        
        try {
            skeletonService.skeletonPurchaseKardex(facture);
            kardexSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonPurchaseWeigthAvarage: {}", e.getMessage());
        }

        try {
            skeletonService.skeletonPurchaseKardexPeps(facture);
            pepsSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonPurchaseWeigthAvarage: {}", e.getMessage());
        }

         
        // Si ninguno de los dos servicios funcionó, lanzar excepción
        if (!kardexSuccess  && !pepsSuccess) {
            throw new RuntimeException("Ambos servicios fallaron: skeletonPurchaseWeigthAverage y skeletonPurchasePeps");
        }


    }

    @PostMapping("/skeleton/sale-for-receipt")
    public void createSaleForReceiptSkeleton(@RequestBody Facture2 facture) {
        boolean receiptSuccess = false;
        boolean kardexSuccess = false;
        boolean pepsSuccess = false;
        
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

        try {
            skeletonService.skeletonSaleKardexPeps(facture);
            pepsSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonSaleKardexPeps: {}", e.getMessage());
        }
        
        
        // Si ninguno de los dos servicios funcionó, lanzar excepción
        if (!receiptSuccess && !kardexSuccess && !pepsSuccess) {
            throw new RuntimeException("Los tres servicios fallaron: skeletonSaleReceipt, skeletonSaleKardex, skeletonSaleKardexPeps ");
        }

    }

    @PostMapping("/skeleton/return-on-sale/{factCode}")
    public void createReturnOnSaleSkeleton(
        @PathVariable Long factCode,
        @RequestBody Product2 product) {

        boolean kardexSuccess = false;
        boolean pepsSuccess = false;

        

        try{
            skeletonService.skeletonReturnOnSaleKardex(factCode, product);
            kardexSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonReturnOnSaleWeigthAvarage: {}", e.getMessage());
        } 

        try{
            skeletonService.skeletonReturnOnSaleKardexPeps(factCode, product);
            pepsSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonReturnOnSalePeps: {}", e.getMessage());
        }


     // Si ninguno de los dos servicios funcionó, lanzar excepción
        if (!kardexSuccess  && !pepsSuccess) {
            throw new RuntimeException("Ambos servicios fallaron: skeletonSaleWeigthAverage y skeletonSalePeps");
        }


    }

    @PostMapping("/skeleton/return-on-purchase/{factCode}")
    public void createReturnOnPurchaseSkeleton(
        @PathVariable Long factCode,
        @RequestBody Product2 product) {

        boolean kardexSuccess = false;
        boolean pepsSuccess = false;

        try{
            skeletonService.skeletonReturnOnPurchaseKardex(factCode, product);
            kardexSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonReturnOnPurchaseWeigthAvarage: {}", e.getMessage());
        }

        try{
            skeletonService.skeletonReturnOnPurchaseKardexPeps(factCode, product);
            kardexSuccess = true;
        } catch (Exception e) {
            log.error("Error en skeletonReturnOnPurchasePeps: {}", e.getMessage());
        }
        // Si ninguno de los dos servicios funcionó, lanzar excepción
        if (!kardexSuccess  && !pepsSuccess) {
            throw new RuntimeException("Ambos servicios fallaron: skeletonSaleWeigthAverage y skeletonSalePeps");
        }

    }

    @PostMapping("/skeleton/non-commercial-entry")
    public void createNonCommercialEntry(@RequestBody Facture2 facture) {
        boolean nonCommercial = false;
        try{
            skeletonService.skeletonNonCommercialEntrytKardexPeps(facture);
            nonCommercial=true;
        }catch(Exception e){
            log.error("Error en skeletonNonCommercialEntry: {}", e.getMessage());
        }
        if(!nonCommercial){
            throw new RuntimeException("Fallo el servicio: skeletonNonCommercialEntry");
        }
    }
    @PostMapping("/skeleton/non-commercial-exit")
    public void createNonCommercialExit(@RequestBody Facture2 facture) {
        boolean nonCommercial = false;
        try {
            skeletonService.skeletonNonCommercialExitKardexPeps(facture);
            nonCommercial=true;
        } catch (Exception e) {
          log.error("Error en skeletonNonCommercialExit: {}", e.getMessage());
        }
        if(!nonCommercial){
            throw new RuntimeException("Fallo el servicio: skeletonNonCommercialExit");
        }
    }
    


    



}
