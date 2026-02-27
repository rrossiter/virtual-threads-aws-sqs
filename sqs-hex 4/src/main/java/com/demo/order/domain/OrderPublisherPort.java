package com.demo.order.domain;

import java.util.concurrent.CompletableFuture;

/**
 * Porta de saída para publicação de eventos.
 *
 * Retorna CompletableFuture para que o domínio possa aguardar
 * a confirmação da entrega antes de liberar o ACK do SQS.
 * O domínio não sabe se é Kafka, SNS ou outro broker.
 */
public interface OrderPublisherPort {
    CompletableFuture<Void> publish(Order order);
}
