package com.demo.sqs.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "product-api", url = "${product.api.url}")
public interface ProductClient {

    @GetMapping("/api/products")
    List<ProductResponse> findAll();
}
