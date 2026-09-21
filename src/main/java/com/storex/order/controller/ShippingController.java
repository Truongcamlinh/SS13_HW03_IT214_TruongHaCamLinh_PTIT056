package com.storex.order.controller;

import com.storex.order.client.ShippingClient;
import com.storex.order.model.ShippingFeeRequest;
import com.storex.order.model.ShippingFeeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders/shipping")
public class ShippingController {
    private final ShippingClient shippingClient;

    public ShippingController(ShippingClient shippingClient) {
        this.shippingClient = shippingClient;
    }

    @PostMapping("/fee")
    public Mono<ShippingFeeResponse> calculateFee(@RequestBody ShippingFeeRequest request) {
        return shippingClient.calculateFee(request);
    }
}

