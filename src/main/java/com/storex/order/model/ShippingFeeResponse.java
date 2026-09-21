package com.storex.order.model;

import java.math.BigDecimal;

public record ShippingFeeResponse(String orderId, BigDecimal fee, String status) {
}

