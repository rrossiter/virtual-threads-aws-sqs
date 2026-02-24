package com.demo.order.adapter.out.product;

import com.demo.order.domain.Product;
import com.demo.order.domain.ProductCatalogPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de saída — implementa ProductCatalogPort via Feign.
 * Converte DTOs da API externa para o modelo de domínio.
 */
@Component
public class ProductCatalogAdapter implements ProductCatalogPort {

    private final ProductFeignClient feignClient;

    public ProductCatalogAdapter(ProductFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public List<Product> findAll() {
        return feignClient.findAll()
            .stream()
            .map(r -> new Product(r.id(), r.name(), r.category(), r.available()))
            .toList();
    }
}
