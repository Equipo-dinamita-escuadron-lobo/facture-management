package com.facturemanagement.infraestructure.adapters.output.messageBroker;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.facturemanagement.application.ports.input.IWeightedAverageEventPort;
import com.facturemanagement.infraestructure.adapters.config.rabbitConfig.RabbitReceiptConfig;
import com.facturemanagement.infraestructure.adapters.config.rabbitConfig.RabbitWeightedAverageConfig;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.EventDto;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import com.facturemanagement.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeightedAverageEventPublisher implements IWeightedAverageEventPort {

    private final RabbitTemplate rabbitTemplate;
    private final IJwtUtils jwtUtils;
 
    @Override
    public void publishPurchaseWeightedAverageEvent(KardexPurchaseDtoRequest kardexDtoRequest) {
        EventDto<KardexPurchaseDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.PURCHASE, kardexDtoRequest);
        log.info("Publishing weighted average event: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.SALE, kardexDtoRequest);
        log.info("Publishing weighted average event: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishReturnOnSaleWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONSALE, kardexDtoRequest);
        log.info("Publishing weighted average event: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishReturnOnPurchaseWeightedAverageEvent(KardexSalesDtoRequest kardexDtoRequest) {
        EventDto<KardexSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.RETURNONPURCHASE, kardexDtoRequest);
        log.info("Publishing weighted average event: {}", kardexDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitWeightedAverageConfig.WEIGHTED_AVERAGE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken()
            ));
            return message;
        });
    }

    @Override
    public void publishSaleReceiptEvent(ReceiptSalesDtoRequest receiptSalesDtoRequest) {
        EventDto<ReceiptSalesDtoRequest, EventFactureType> event = new EventDto<>(EventFactureType.SALE, receiptSalesDtoRequest);
        log.info("Publishing RECEIPT event: {}", receiptSalesDtoRequest.getFactCode());

        rabbitTemplate.convertAndSend(RabbitReceiptConfig.INVOICE_EXCHANGE, "", event, message -> {
            message.getMessageProperties().setHeaders(Map.of(
                    "x-jwt-token", jwtUtils.getToken(),
                    "x-tenant-id", jwtUtils.getId()
            ));
            return message;
        });
    }

}
