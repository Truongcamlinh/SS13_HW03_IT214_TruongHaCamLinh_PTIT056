package com.storex.order.model;

public record ShippingFeeRequest(String orderId, String destination, double weight) {
}

