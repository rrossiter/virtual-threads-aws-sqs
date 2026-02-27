package com.demo.order.domain;

import java.util.UUID;

/**
 * Entidade central do domínio.
 * Sem dependências de frameworks — pode ser testada em isolamento total.
 */
public class Order {

    private final String orderId;
    private final String product;
    private final int quantity;
    private final double totalPrice;
    private String category;
    private boolean productAvailable;

    private Order(String orderId, String product, int quantity, double totalPrice) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public static Order of(String product, int quantity, double totalPrice) {
        return new Order(UUID.randomUUID().toString(), product, quantity, totalPrice);
    }

    public static Order restore(String orderId, String product, int quantity, double totalPrice) {
        return new Order(orderId, product, quantity, totalPrice);
    }

    public void enrich(String category, boolean available) {
        this.category = category;
        this.productAvailable = available;
    }

    public boolean isEnriched()       { return category != null; }
    public String orderId()           { return orderId; }
    public String product()           { return product; }
    public int quantity()             { return quantity; }
    public double totalPrice()        { return totalPrice; }
    public String category()          { return category; }
    public boolean productAvailable() { return productAvailable; }
}
