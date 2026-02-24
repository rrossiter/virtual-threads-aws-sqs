package com.demo.order.adapter.out.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Cliente HTTP Feign — detalhe de infraestrutura, invisível ao domínio.
 */
@FeignClient(name = "product-api", url = "${product.api.url}")
public interface ProductFeignClient {

    @GetMapping("/api/products")
    List<ProductApiResponse> findAll();
}
