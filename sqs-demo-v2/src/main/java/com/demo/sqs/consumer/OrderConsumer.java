package com.demo.sqs.consumer;

import com.demo.sqs.client.ProductClient;
import com.demo.sqs.client.ProductResponse;
import com.demo.sqs.kafka.OrderKafkaProducer;
import com.demo.sqs.model.OrderEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final ProductClient productClient;
    private final OrderKafkaProducer kafkaProducer;

    public OrderConsumer(ProductClient productClient, OrderKafkaProducer kafkaProducer) {
        this.productClient = productClient;
        this.kafkaProducer = kafkaProducer;
    }

    @SqsListener(value = "${app.queue.name}", factory = "defaultSqsListenerContainerFactory")
    public void listen(List<Message<OrderEvent>> messages) {
        log.info("=== Batch recebido do SQS: {} mensagens ===", messages.size());

        // 1. Busca catálogo de produtos via Feign (MockServer)
        List<ProductResponse> produtos = productClient.findAll();
        Map<String, ProductResponse> catalogo = produtos.stream()
            .collect(Collectors.toMap(ProductResponse::name, p -> p));

        log.info("Catálogo carregado: {} produtos", produtos.size());

        // 2. Processa cada mensagem e publica no Kafka
        for (Message<OrderEvent> message : messages) {
            try {
                OrderEvent event = message.getPayload();

                log.info("  → Processando orderId={} | produto={} | qtd={} | total=R${}",
                    event.orderId(), event.product(), event.quantity(), event.totalPrice());

                // Enriquece com dados do catálogo
                ProductResponse produto = catalogo.get(event.product());
                if (produto != null) {
                    log.info("    [Catálogo] categoria={} | disponível={}",
                        produto.category(), produto.available());
                } else {
                    log.warn("    [Catálogo] produto '{}' não encontrado", event.product());
                }

                // 3. Publica no tópico Kafka
                kafkaProducer.send(event);

            } catch (Exception ex) {
                log.error("Erro ao processar mensagem id={}: {}",
                    message.getHeaders().getId(), ex.getMessage(), ex);
            }
        }

        log.info("=== Batch finalizado: {} mensagens encaminhadas ao Kafka ===", messages.size());
    }
}
