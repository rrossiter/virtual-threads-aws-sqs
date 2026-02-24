package com.demo.order.adapter.out.kafka;

import com.demo.order.domain.Order;

/**
 * DTO de saída para o tópico Kafka — inclui dados enriquecidos.
 */
public record OrderKafkaPayload(
    String orderId,
    String product,
    int quantity,
    double totalPrice,
    String category,
    boolean productAvailable
) {
    public static OrderKafkaPayload from(Order order) {
        return new OrderKafkaPayload(
            order.orderId(),
            order.product(),
            order.quantity(),
            order.totalPrice(),
            order.category(),
            order.productAvailable()
        );
    }
}
