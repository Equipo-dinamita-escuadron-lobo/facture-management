package com.facturemanagement.infraestructure.adapters.config.rabbitConfig;

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
    public static final String INVOICE_ACCOUNTING_QUEUE = "invoice.accounting.queue";

    // Primary and message exchange dead for third invoices
    @Bean
    FanoutExchange invoiceExchange() {
        return new FanoutExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    Queue invoicePaymentsQueue() {
        return QueueBuilder.durable(INVOICE_PAYMENTS_QUEUE).build();
    }

    @Bean
    Binding invoicePaymentsBinding() {
        return BindingBuilder.bind(invoicePaymentsQueue()).to(invoiceExchange());
    }

    @Bean
    Queue invoiceAccountingQueue() {
        return QueueBuilder.durable(INVOICE_ACCOUNTING_QUEUE).build();
    }

    @Bean
    Binding invoiceAccountingBinding() {
        return BindingBuilder.bind(invoiceAccountingQueue()).to(invoiceExchange());
    }
}
