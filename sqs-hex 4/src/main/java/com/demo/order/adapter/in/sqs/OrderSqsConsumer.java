package com.demo.order.adapter.in.sqs;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.BatchAcknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de entrada — recebe batch do SQS e controla ACK individual.
 *
 * Fluxo:
 *   1. Converte DTOs → entidades de domínio
 *   2. Delega ao OrderService que processa todas em paralelo (StructuredTaskScope)
 *   3. Recebe de volta apenas as mensagens que tiveram Kafka ACK
 *   4. Faz ACK somente nas bem-sucedidas
 *   5. As que falharam não recebem ACK → voltam à fila após visibility timeout
 */
@Component
public class OrderSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderSqsConsumer.class);

    private final OrderService orderService;

    public OrderSqsConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @SqsListener(value = "${app.queue.name}", factory = "defaultSqsListenerContainerFactory")
    public void consume(
        List<Message<OrderSqsMessage>> messages,
        BatchAcknowledgement<OrderSqsMessage> ack
    ) {
        log.info("=== Batch recebido do SQS: {} mensagens ===", messages.size());

        List<Order> orders = messages.stream()
            .map(msg -> {
                OrderSqsMessage dto = msg.getPayload();
                return Order.restore(dto.orderId(), dto.product(), dto.quantity(), dto.totalPrice());
            })
            .toList();

        // Processa todas — retorna apenas as que tiveram Kafka ACK
        List<Message<OrderSqsMessage>> sucessos = orderService.processBatch(orders, messages);

        int falhas = messages.size() - sucessos.size();

        if (!sucessos.isEmpty()) {
            ack.acknowledge(sucessos);
            log.info("SQS ACK → {} mensagens confirmadas", sucessos.size());
        }

        if (falhas > 0) {
            log.warn("SQS NACK → {} mensagens voltarão à fila após visibility timeout", falhas);
        }
    }
}
