package com.karan.inventory_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Collections;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            @Value("${app.kafka.retry-backoff-ms}") long retryBackoffMs) {

        // Local learning setup: failure par unlimited retry
        FixedBackOff backOff = new FixedBackOff(
                retryBackoffMs,
                FixedBackOff.UNLIMITED_ATTEMPTS
        );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(backOff);

        // Default fatal classifications replace karke sab exceptions retry karenge
        errorHandler.setClassifications(Collections.emptyMap(), true);

        return errorHandler;
    }
}