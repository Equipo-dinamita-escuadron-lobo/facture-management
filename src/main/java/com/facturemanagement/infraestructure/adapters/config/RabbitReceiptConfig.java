package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Profile("!test")
public class RabbitReceiptConfig {
    //Constants for invoice
    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String INVOICE_PAYMENTS_QUEUE = "invoice.payments.queue";
    public static final String INVOICE_PAYMENTS_DLX = "invoice.payments.dlx";
    public static final String INVOICE_PAYMENTS_DLQ = "invoice.payments.dlq";
    public static final String INVOICE_PAYMENTS_RETRY_QUEUE = "invoice.payments.retry.queue";

    // Primary and message exchange dead for third invoices
    @Bean
    FanoutExchange invoiceExchange() {
        return new FanoutExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    FanoutExchange invoicePaymentsDlx() {
        return new FanoutExchange(INVOICE_PAYMENTS_DLX, true, false);
    }

    // Invoice Queues and Bindings
    @Bean
    Queue invoicePaymentsQueue() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", INVOICE_PAYMENTS_DLX)
                .build();
    }

    @Bean
    Queue invoicePaymentsDlq() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_DLQ).build();
    }

    @Bean
    Queue invoicePaymentsRetryQueue() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_RETRY_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minuto de espera para reintento
                .withArgument("x-dead-letter-exchange", INVOICE_PAYMENTS_DLX) // Si falla después de reintento, va al DLX
                .build();
    }

    @Bean
    Binding invoicePaymentsBinding() {
        return BindingBuilder.bind(invoicePaymentsQueue()).to(invoiceExchange());
    }

    @Bean
    Binding invoicePaymentsDlqBinding() {
        return BindingBuilder.bind(invoicePaymentsDlq()).to(invoicePaymentsDlx());
    }

    @Bean
    Binding invoicePaymentsRetryBinding() {
        return BindingBuilder.bind(invoicePaymentsRetryQueue()).to(invoicePaymentsDlx());
    }
}
