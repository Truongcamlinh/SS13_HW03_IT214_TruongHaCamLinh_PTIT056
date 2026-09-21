package com.storex.order;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShippingCircuitBreakerTest {
    @Autowired
    private CircuitBreakerRegistry registry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = registry.circuitBreaker("shippingClient");
        circuitBreaker.reset();
    }

    @Test
    void loadsSlaConfiguration() {
        CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();

        assertThat(config.getSlidingWindowType())
                .isEqualTo(CircuitBreakerConfig.SlidingWindowType.TIME_BASED);
        assertThat(config.getSlidingWindowSize()).isEqualTo(30);
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1)).isEqualTo(20_000L);
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
        assertThat(config.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
    }

    @Test
    void permitsOnlyThreeCallsInHalfOpenState() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();

        long accepted = IntStream.range(0, 10)
                .filter(index -> circuitBreaker.tryAcquirePermission())
                .count();

        assertThat(accepted).isEqualTo(3);
        assertThat(circuitBreaker.getMetrics().getNumberOfNotPermittedCalls()).isEqualTo(7);
    }
}

