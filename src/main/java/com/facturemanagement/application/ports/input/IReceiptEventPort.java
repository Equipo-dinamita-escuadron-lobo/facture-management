package com.facturemanagement.application.ports.input;

import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;

public interface IReceiptEventPort {
        void publishSaleReceiptEvent(ReceiptSalesDtoRequest receiptSalesDtoRequest);
        void publishPurchaseInvoiceEvent(PurchaseInvoiceEventDto event);
}
