package com.facturemanagement.infraestructure.adapters.output.messageBroker;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.facturemanagement.application.ports.input.IpepsEventPort;
import com.facturemanagement.infraestructure.adapters.config.RabbitPEPSConfig;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.EventDto;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PepsEventPublisher implements IpepsEventPort {
    
    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;
    
    @Override
    public void publishPurchasePEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        EventDto<KardexPurchaseDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.PURCHASE, kardexDtoRequest); 
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());
        
        rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.SALE, kardexDtoRequest);
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());
        rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });

    }

    @Override
    public void publishReturnOnSalePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONSALE, kardexDtoRequest);
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());
         rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishReturnOnPurchasePEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONPURCHASE, kardexDtoRequest);
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishNonCommercialExitPEPSEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.NONCOMMERCIALEXIT, kardexDtoRequest);
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());
        rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishNonCommercialEntryPEPSEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
         EventDto<KardexPurchaseDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.NONCOMMERCIALENTRY, kardexDtoRequest); 
        log.info("Publishing peps event: {}", kardexDtoRequest.getFactCode());
        
        rabbitTemplate.convertAndSend(RabbitPEPSConfig.PEPS_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }
    
}
