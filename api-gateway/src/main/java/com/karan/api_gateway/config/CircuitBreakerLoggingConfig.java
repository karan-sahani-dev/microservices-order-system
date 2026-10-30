package com.karan.api_gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerLoggingConfig {
    private static final Logger log =
            LoggerFactory.getLogger(CircuitBreakerLoggingConfig.class);
    @Bean
    public ApplicationRunner circuitBreakerLogging(CircuitBreakerRegistry registry){
        return args -> {
            CircuitBreaker circuitBreaker = registry.circuitBreaker("inventoryCircuitBreaker");
            circuitBreaker.getEventPublisher()
                    .onStateTransition(event -> log.info(
                            "Ciruit breaker {} changed state: {}",
                            event.getCircuitBreakerName(),
                            event.getStateTransition()
                    ));
        };
    }
}
