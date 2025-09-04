package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitWeightedAverageConfig {
    public static final String WEIGHTED_AVERAGE_EXCHANGE = "weighted.average.exchange";
    public static final String WEIGHTED_AVERAGE_QUEUE = "weighted.average.queue";

    // Dead Letter Queue configuration
    public static final String WEIGHTED_AVERAGE_DLQ = "weighted.average.dlq";
    public static final String WEIGHTED_AVERAGE_DLX = "weighted.average.dlx";

    // Dead Letter Exchange
    @Bean
    FanoutExchange weightedAverageDlx() {
        return new FanoutExchange(WEIGHTED_AVERAGE_DLX, true, false);
    }

    // Dead Letter Queue - for domain/business rule violations (no retry)
    @Bean
    Queue weightedAverageDlq() {
        return QueueBuilder.durable(WEIGHTED_AVERAGE_DLQ).build();
    }

    // Main Queue with DLQ configuration - for critical order processing
    @Bean
    Queue weightedAverageQueue() {
        return QueueBuilder.durable(WEIGHTED_AVERAGE_QUEUE)
                .withArgument("x-dead-letter-exchange", WEIGHTED_AVERAGE_DLX)
                .withArgument("x-dead-letter-routing-key", "")
                // Reduced TTL for faster processing as order matters for weighted average
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    @Bean
    FanoutExchange productExchange() {
        return new FanoutExchange(WEIGHTED_AVERAGE_EXCHANGE, true, false);
    }

    @Bean
    Binding weightedAverageQueueBinding() {
        return BindingBuilder.bind(weightedAverageQueue()).to(productExchange());
    }

    @Bean
    Binding weightedAverageDlqBinding() {
        return BindingBuilder.bind(weightedAverageDlq()).to(weightedAverageDlx());
    }

}
