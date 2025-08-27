package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitCommonConfig {
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Custom RabbitTemplate with proper error handling
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        
        // Return callback para mensajes no enrutables
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned.getMessage());
            log.error("Reply code: {}", returned.getReplyCode());
            log.error("Reply text: {}", returned.getReplyText());
            log.error("Exchange: {}", returned.getExchange());
            log.error("Routing key: {}", returned.getRoutingKey());
        });
        
        // Confirm callback para publishers
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message not delivered to exchange. Cause:: {}", cause);
            }
        });
        
        return template;
    }

    // Custom listener container factory
    @Bean
    RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
