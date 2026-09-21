package com.storex.order.client;

import com.storex.order.model.ShippingFeeRequest;
import com.storex.order.model.ShippingFeeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ShippingClient {
    private final WebClient webClient;

    public ShippingClient(WebClient.Builder builder,
                          @Value("${shipping.api.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @CircuitBreaker(name = "shippingClient", fallbackMethod = "feeFallback")
    public Mono<ShippingFeeResponse> calculateFee(ShippingFeeRequest request) {
        return webClient.post()
                .uri("/api/shipping/fee")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ShippingFeeResponse.class);
    }

    private Mono<ShippingFeeResponse> feeFallback(
            ShippingFeeRequest request,
            Throwable error) {
        return Mono.just(new ShippingFeeResponse(
                request.orderId(), BigDecimal.ZERO, "SHIPPING_UNAVAILABLE"));
    }
}

