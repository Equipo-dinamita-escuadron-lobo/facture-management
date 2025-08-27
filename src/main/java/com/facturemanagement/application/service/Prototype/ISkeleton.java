package com.facturemanagement.application.service.Prototype;

public interface ISkeleton {
    void skeletonPurchaseKardex(Facture2 facture);
    void skeletonSaleKardex(Facture2 facture);
    void skeletonReturnOnSaleKardex(Long factCode, Product2 product);
    void skeletonReturnOnPurchaseKardex(Long factCode, Product2 product);

}
