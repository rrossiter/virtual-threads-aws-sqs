package com.demo.sqs.model;

import java.util.UUID;

public record OrderEvent(
    String orderId,
    String product,
    int quantity,
    double totalPrice
) {
    public static OrderEvent of(String product, int quantity, double price) {
        return new OrderEvent(UUID.randomUUID().toString(), product, quantity, price);
    }
}
