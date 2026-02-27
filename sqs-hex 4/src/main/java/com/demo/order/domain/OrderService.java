package com.demo.order.domain;

import com.demo.order.adapter.in.sqs.OrderSqsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

/**
 * Serviço de domínio — processa batch com StructuredTaskScope + Virtual Threads.
 *
 * Cada mensagem do batch é processada em uma VT independente.
 * Falhas individuais não cancelam o restante do batch.
 * Ao final, uma única passagem sobre List<OrderTask> separa
 * sucessos de falhas — sem Map, sem correlação por índice.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final ProductCatalogPort productCatalogPort;
    private final OrderPublisherPort orderPublisherPort;

    public OrderService(
        ProductCatalogPort productCatalogPort,
        OrderPublisherPort orderPublisherPort
    ) {
        this.productCatalogPort = productCatalogPort;
        this.orderPublisherPort = orderPublisherPort;
    }

    /**
     * Agrupa mensagem SQS + subtask numa estrutura plana.
     * Elimina a necessidade de correlação por índice após o join().
     */
    private record OrderTask(
        Message<OrderSqsMessage> message,
        StructuredTaskScope.Subtask<Void> subtask
    ) {}

    /**
     * Processa todas as mensagens do batch em paralelo.
     * Retorna apenas as mensagens cujo envio ao Kafka foi confirmado.
     *
     * @param orders   entidades de domínio
     * @param messages mensagens originais do SQS (necessárias para o ACK)
     * @return mensagens com Kafka ACK confirmado
     */
    public List<Message<OrderSqsMessage>> processBatch(
        List<Order> orders,
        List<Message<OrderSqsMessage>> messages
    ) {
        log.info("Processando batch de {} pedidos", orders.size());

        Map<String, Product> catalog = loadCatalog();

        // Capacidade pré-alocada — sem rehash durante o fork
        List<OrderTask> tasks = new ArrayList<>(orders.size());

        try (var scope = new StructuredTaskScope<Void>()) {

            for (int i = 0; i < orders.size(); i++) {
                Order order   = orders.get(i);
                Message<OrderSqsMessage> message = messages.get(i);

                StructuredTaskScope.Subtask<Void> subtask = scope.fork(() -> {
                    enrich(order, catalog);
                    // .get() na Virtual Thread — suspende a VT, libera Platform Thread
                    orderPublisherPort.publish(order).get();
                    log.info("Kafka ACK → orderId={} | produto={} | categoria={}",
                        order.orderId(), order.product(), order.category());
                    return null;
                });

                // Mensagem e subtask agrupados — correlação direta, sem índice
                tasks.add(new OrderTask(message, subtask));
            }

            // Aguarda TODAS as VTs — sem cancelar na primeira falha
            scope.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Batch interrompido", e);
        }

        // Uma única passagem — particiona sucessos e falhas
        List<Message<OrderSqsMessage>> sucessos = new ArrayList<>(tasks.size());

        for (OrderTask task : tasks) {
            if (task.subtask().state() == StructuredTaskScope.Subtask.State.SUCCESS) {
                sucessos.add(task.message());
            } else {
                log.error("Kafka NACK → mensagem voltará à fila | erro={}",
                    task.subtask().exception().getMessage());
            }
        }

        log.info("Batch finalizado → ACK: {} | NACK: {}", sucessos.size(), tasks.size() - sucessos.size());
        return sucessos;
    }

    private Map<String, Product> loadCatalog() {
        List<Product> products = productCatalogPort.findAll();
        log.info("Catálogo carregado: {} produtos", products.size());
        return products.stream().collect(Collectors.toMap(Product::name, p -> p));
    }

    private void enrich(Order order, Map<String, Product> catalog) {
        Product product = catalog.get(order.product());
        if (product != null) {
            order.enrich(product.category(), product.available());
        } else {
            log.warn("Produto '{}' não encontrado no catálogo → orderId={}", order.product(), order.orderId());
            order.enrich("DESCONHECIDO", false);
        }
    }
}
