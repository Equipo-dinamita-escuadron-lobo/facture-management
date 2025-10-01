package com.facturemanagement.application.ports.input;

import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;

public interface IpepsEventPort {
    void publishPurchasePEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest);
    void publishSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishReturnOnPurchasePEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishNonCommercialExitPEPSEvent(KardexSalesDtoRequest kardexDtoRequest);
    void publishNonCommercialEntryPEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest);

}
