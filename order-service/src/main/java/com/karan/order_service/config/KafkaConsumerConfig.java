package com.karan.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Collections;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        FixedBackOff backOff = new FixedBackOff(
                2000L,
                FixedBackOff.UNLIMITED_ATTEMPTS
        );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(backOff);

        // Abhi sab listener exceptions retry karenge
        errorHandler.setClassifications(
                Collections.emptyMap(),
                true
        );

        return errorHandler;
    }
}