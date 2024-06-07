package com.facturemanagement.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.facturemanagement.application.ports.input.ListFactureUseCase;
import com.facturemanagement.application.ports.output.FactureGetOutputPort;
import com.facturemanagement.domain.exception.FacturesNotFound;
import com.facturemanagement.domain.model.Facture;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListFacturesService implements ListFactureUseCase{
    private final FactureGetOutputPort factureGetOutputPort;

    @Override
    public Page<Facture> getAllFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllFacturesBy(entId, pageable);
        if(result.isEmpty()){
            throw new FacturesNotFound("No factures found for enterprise id "+entId);
        }
        return result;
    }

    @Override
    public Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllSalesFacturesBy(entId, pageable);
        if(result.isEmpty()){
            throw new FacturesNotFound("No sales factures found for enterprise id "+entId);
        }
        return result;
    }

    @Override
    public Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllShoppingFacturesBy(entId, pageable);
        if(result.isEmpty()){
            throw new FacturesNotFound("No shopping factures found for enterprise id "+entId);
        }
        return result;
    }
}
