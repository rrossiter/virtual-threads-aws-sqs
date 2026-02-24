package com.demo.order.adapter.out.kafka;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de saída — implementa OrderPublisherPort via Kafka.
 * O domínio não sabe que existe Kafka; depende apenas da interface.
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
    public void publish(Order order) {
        OrderKafkaPayload payload = OrderKafkaPayload.from(order);

        CompletableFuture<SendResult<String, OrderKafkaPayload>> future =
            kafkaTemplate.send(topic, order.orderId(), payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Falha ao publicar no Kafka → orderId={} | erro={}",
                    order.orderId(), ex.getMessage());
            } else {
                log.info("Publicado no Kafka → tópico={} | partição={} | offset={} | orderId={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    order.orderId());
            }
        });
    }
}
