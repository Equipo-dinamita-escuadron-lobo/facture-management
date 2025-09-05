package com.facturemanagement.application.service.Prototype;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.facturemanagement.application.ports.input.IWeightedAverageEventPort;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Skeleton implements ISkeleton {

    private final IWeightedAverageEventPort weightedAverageEventPort;

    @Override
    public void skeletonPurchaseKardex(Facture2 facture) {

        Long factCode = facture.getFactCode();
        Set<Product2> products = facture.getFactProducts();

        for (Product2 product : products) {
            KardexPurchaseDtoRequest kardexDtoRequest = new KardexPurchaseDtoRequest();
            kardexDtoRequest.setQuantity(product.getAmount());
            kardexDtoRequest.setFactCode(factCode);

            double basePrice = product.getBasePrice();

            kardexDtoRequest.setUnitPrice(BigDecimal.valueOf(basePrice));
            kardexDtoRequest.setProductId(product.getProductId());
            //kardexDtoRequest.setDetails("Factura " + factCode); // No es obligatorio

            weightedAverageEventPort.publishPurchaseWeightedAverageEvent(kardexDtoRequest);
        }
    }

    @Override
    public void skeletonSaleKardex(Facture2 facture) {
        Long factCode = facture.getFactCode();
        Set<Product2> products = facture.getFactProducts();

        for (Product2 product : products) {
            KardexSalesDtoRequest kardexDtoRequest = new KardexSalesDtoRequest();
            kardexDtoRequest.setQuantity(product.getAmount());
            kardexDtoRequest.setFactCode(factCode);
            kardexDtoRequest.setProductId(product.getProductId());
            //kardexDtoRequest.setDetails("Factura " + factCode); // No es obligatorio

            weightedAverageEventPort.publishSaleWeightedAverageEvent(kardexDtoRequest);
        }
    }

    @Override
    public void skeletonReturnOnSaleKardex(Long factCode, Product2 product) {
        Long productId = product.getProductId();
        KardexSalesDtoRequest kardexDtoRequest = new KardexSalesDtoRequest();
        kardexDtoRequest.setQuantity(product.getAmount());
        kardexDtoRequest.setFactCode(factCode);
        kardexDtoRequest.setProductId(productId);
        //kardexDtoRequest.setDetails("Factura " + factCode); // No es obligatorio

        weightedAverageEventPort.publishReturnOnSaleWeightedAverageEvent(kardexDtoRequest);
    }

    @Override
    public void skeletonReturnOnPurchaseKardex(Long factCode, Product2 product) {
        Long productId = product.getProductId();
        KardexSalesDtoRequest kardexDtoRequest = new KardexSalesDtoRequest();
        kardexDtoRequest.setQuantity(product.getAmount());
        kardexDtoRequest.setFactCode(factCode);
        kardexDtoRequest.setProductId(productId);
        //kardexDtoRequest.setDetails("Factura " + factCode); // No es obligatorio

        weightedAverageEventPort.publishReturnOnPurchaseWeightedAverageEvent(kardexDtoRequest);
    }

    @Override
    public void skeletonSaleReceipt(Facture2 facture) {
        ReceiptSalesDtoRequest receiptSalesDtoRequest = new ReceiptSalesDtoRequest();
        receiptSalesDtoRequest.setFactCode(facture.getFactCode());
        receiptSalesDtoRequest.setEntId(facture.getEntId());
        receiptSalesDtoRequest.setThirdId(facture.getThId());
        receiptSalesDtoRequest.setTotalPay(Long.valueOf(facture.getTotalPay()));
        receiptSalesDtoRequest.setTotalValue(Long.valueOf(facture.getTotalValue()));
        receiptSalesDtoRequest.setPendingValue(Long.valueOf(facture.getPendingValue()));
        receiptSalesDtoRequest.setExpirationDate(facture.getExpirationDate());
        receiptSalesDtoRequest.setActive(true);
        
        weightedAverageEventPort.publishSaleReceiptEvent(receiptSalesDtoRequest);
    }

    
}
