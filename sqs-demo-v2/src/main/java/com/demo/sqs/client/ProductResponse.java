package com.demo.sqs.client;

public record ProductResponse(
    Long id,
    String name,
    String category,
    boolean available
) {}
