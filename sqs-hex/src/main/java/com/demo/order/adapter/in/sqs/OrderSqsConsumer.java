package com.demo.order.adapter.in.sqs;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de entrada — recebe mensagens em batch do SQS,
 * converte para o modelo de domínio e delega ao OrderService.
 * Sem lógica de negócio aqui.
 */
@Component
public class OrderSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderSqsConsumer.class);

    private final OrderService orderService;

    public OrderSqsConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @SqsListener(value = "${app.queue.name}", factory = "defaultSqsListenerContainerFactory")
    public void consume(List<Message<OrderSqsMessage>> messages) {
        log.info("=== Batch recebido do SQS: {} mensagens ===", messages.size());

        List<Order> orders = messages.stream()
            .map(msg -> {
                OrderSqsMessage dto = msg.getPayload();
                return Order.restore(dto.orderId(), dto.product(), dto.quantity(), dto.totalPrice());
            })
            .toList();

        orderService.processBatch(orders);
    }
}
