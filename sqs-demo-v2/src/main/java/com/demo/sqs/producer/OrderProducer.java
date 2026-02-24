package com.demo.sqs.producer;

import com.demo.sqs.model.OrderEvent;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final SqsTemplate sqsTemplate;

    @Value("${app.queue.name}")
    private String queueName;

    public OrderProducer(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public void send(OrderEvent event) {
        log.info("Enviando pedido para fila: {}", event);

        sqsTemplate.send(to -> to
            .queue(queueName)
            .payload(event)
            .header("eventType", "ORDER_CREATED")
        );

        log.info("Pedido enviado com sucesso! orderId={}", event.orderId());
    }
}
