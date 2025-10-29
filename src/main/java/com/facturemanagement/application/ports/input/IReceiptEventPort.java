package com.facturemanagement.application.ports.input;

import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;

public interface IReceiptEventPort {
        void publishSaleReceiptEvent(ReceiptSalesDtoRequest receiptSalesDtoRequest);
}
