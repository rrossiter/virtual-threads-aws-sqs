package com.demo.order.domain;

import java.util.List;

/**
 * Porta de saída — o domínio define o contrato,
 * a infraestrutura (Feign) fornece a implementação.
 */
public interface ProductCatalogPort {
    List<Product> findAll();
}
