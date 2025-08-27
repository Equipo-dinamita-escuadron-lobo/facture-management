package com.facturemanagement.application.ports.input;

import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;

public interface IWeightedAverageEventPort {
    void publishPurchaseWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest);
    void publishSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnPurchaseWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest);
}
