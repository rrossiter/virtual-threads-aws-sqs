package com.demo.order.adapter.in.sqs;

/**
 * DTO de infraestrutura — representa o payload JSON recebido da fila SQS.
 */
public record OrderSqsMessage(
    String orderId,
    String product,
    int quantity,
    double totalPrice
) {}
