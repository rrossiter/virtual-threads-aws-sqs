package com.demo.sqs.kafka;

import com.demo.sqs.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topic.orders}")
    private String topic;

    public OrderKafkaProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envia um OrderEvent ao tópico kafka-orders-received.
     * A chave é o orderId — garante que pedidos do mesmo order
     * vão sempre para a mesma partição (ordenação garantida).
     */
    public void send(OrderEvent event) {
        CompletableFuture<SendResult<String, OrderEvent>> future =
            kafkaTemplate.send(topic, event.orderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Erro ao publicar orderId={} no Kafka: {}", event.orderId(), ex.getMessage());
            } else {
                log.info("Publicado no Kafka → tópico={} | partição={} | offset={} | orderId={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.orderId());
            }
        });
    }
}
