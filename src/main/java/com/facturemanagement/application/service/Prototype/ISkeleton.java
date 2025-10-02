package com.facturemanagement.application.service.Prototype;

public interface ISkeleton {
    void skeletonPurchaseKardex(Facture2 facture);
    void skeletonSaleKardex(Facture2 facture);
    void skeletonReturnOnSaleKardex(Long factCode, Product2 product);
    void skeletonReturnOnPurchaseKardex(Long factCode, Product2 product);
    void skeletonSaleReceipt(Facture2 facture);

    //methods for inventory PEPS
    void skeletonPurchaseKardexPeps(Facture2 facture);
    void skeletonSaleKardexPeps(Facture2 facture);
    void skeletonReturnOnSaleKardexPeps(Long factCode, Product2 product);
    void skeletonReturnOnPurchaseKardexPeps(Long factCode, Product2 product);
    void skeletonNonCommercialExitKardexPeps(Facture2 facture);
    void skeletonNonCommercialEntrytKardexPeps(Facture2 facture);


}
