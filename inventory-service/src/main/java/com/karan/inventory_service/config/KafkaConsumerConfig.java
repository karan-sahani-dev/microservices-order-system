package com.karan.inventory_service.config;

import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.retry-backoff-ms}") long retryBackoffMs) {

        // Failed record ko DLT ke same partition mein bhejo.
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        recoverer.setFailIfSendResultIsError(true);

        // Pehle attempt ke baad maximum 2 retries.
        var errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(retryBackoffMs, 2L)
        );

        // Invalid JSON ya validation failure par retry skip karo.
        errorHandler.addNotRetryableExceptions(
                JacksonException.class,
                ConstraintViolationException.class
        );

        return errorHandler;
    }
}