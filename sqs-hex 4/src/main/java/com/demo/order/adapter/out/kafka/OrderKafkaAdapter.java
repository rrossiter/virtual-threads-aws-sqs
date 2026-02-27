package com.demo.order.adapter.out.kafka;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de saída — implementa OrderPublisherPort via Kafka.
 *
 * Retorna CompletableFuture<Void> para que quem chamou possa
 * aguardar a confirmação da entrega (acks=all) antes de fazer o ACK no SQS.
 * O bloqueio acontece na Virtual Thread — barato e seguro.
 */
@Component
public class OrderKafkaAdapter implements OrderPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaAdapter.class);

    private final KafkaTemplate<String, OrderKafkaPayload> kafkaTemplate;

    @Value("${kafka.topic.orders}")
    private String topic;

    public OrderKafkaAdapter(KafkaTemplate<String, OrderKafkaPayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<Void> publish(Order order) {
        OrderKafkaPayload payload = OrderKafkaPayload.from(order);

        return kafkaTemplate
            .send(topic, order.orderId(), payload)
            .thenAccept(result ->
                log.info("Kafka ACK → tópico={} | partição={} | offset={} | orderId={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    order.orderId())
            )
            .exceptionally(ex -> {
                log.error("Kafka NACK → orderId={} | erro={}", order.orderId(), ex.getMessage());
                // propaga a exceção para que o OrderService trate a falha
                throw new RuntimeException("Falha ao publicar orderId=" + order.orderId(), ex);
            });
    }
}
