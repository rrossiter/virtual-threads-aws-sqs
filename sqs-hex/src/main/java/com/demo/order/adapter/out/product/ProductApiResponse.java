package com.demo.order.adapter.out.product;

/**
 * DTO de infraestrutura — representa o JSON retornado pela API de produtos.
 */
public record ProductApiResponse(
    Long id,
    String name,
    String category,
    boolean available
) {}
