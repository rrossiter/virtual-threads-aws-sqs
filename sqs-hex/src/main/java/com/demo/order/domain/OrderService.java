package com.demo.order.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço de domínio — contém toda a lógica de negócio.
 *
 * Depende apenas de interfaces (portas de saída) definidas
 * no próprio domínio. Não sabe se o catálogo vem de HTTP,
 * nem se o evento vai para Kafka ou outro broker.
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
     * Processa um lote de pedidos:
     * 1. Carrega catálogo de produtos
     * 2. Enriquece cada pedido com dados do catálogo
     * 3. Publica cada pedido no canal de eventos
     */
    public void processBatch(List<Order> orders) {
        log.info("Processando lote de {} pedidos", orders.size());

        Map<String, Product> catalog = loadCatalog();

        for (Order order : orders) {
            try {
                enrich(order, catalog);
                orderPublisherPort.publish(order);
                log.info("Pedido publicado → orderId={} | produto={} | categoria={} | disponível={}",
                    order.orderId(), order.product(), order.category(), order.productAvailable());
            } catch (Exception ex) {
                log.error("Falha ao processar orderId={}: {}", order.orderId(), ex.getMessage(), ex);
            }
        }

        log.info("Lote concluído: {} pedidos processados", orders.size());
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
            log.warn("Produto '{}' não encontrado no catálogo", order.product());
            order.enrich("DESCONHECIDO", false);
        }
    }
}
