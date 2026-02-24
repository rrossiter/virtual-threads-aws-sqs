package com.demo.order.domain;

/**
 * Value Object representando um produto do catálogo.
 */
public record Product(
    Long id,
    String name,
    String category,
    boolean available
) {}
