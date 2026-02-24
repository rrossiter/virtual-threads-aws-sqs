package com.demo.order.domain;

/**
 * Porta de saída — o domínio define o contrato,
 * a infraestrutura (Kafka) fornece a implementação.
 */
public interface OrderPublisherPort {
    void publish(Order order);
}
