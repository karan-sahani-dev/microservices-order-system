package com.karan.order_service.messaging;

import com.karan.order_service.event.InventoryRejectedEvent;
import com.karan.order_service.event.InventoryReservedEvent;
import com.karan.order_service.service.OrderOutcomeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryOutcomeListener {

    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final OrderOutcomeService outcomeService;

    @KafkaListener(
            topics = "${app.kafka.topics.inventory-reserved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeReserved(String message) throws Exception {

        InventoryReservedEvent event = readAndValidate(
                message,
                InventoryReservedEvent.class
        );

        outcomeService.confirmOrder(event);

        log.info(
                "Order confirmed: orderId={}",
                event.getOrderId()
        );
    }

    @KafkaListener(
            topics = "${app.kafka.topics.inventory-rejected}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeRejected(String message) throws Exception {

        InventoryRejectedEvent event = readAndValidate(
                message,
                InventoryRejectedEvent.class
        );

        outcomeService.cancelOrder(event);

        log.info(
                "Order cancelled: orderId={}, reason={}",
                event.getOrderId(),
                event.getReason()
        );
    }

    private <T> T readAndValidate(
            String message,
            Class<T> eventType) throws Exception {

        T event = jsonMapper.readValue(message, eventType);

        if (event == null) {
            throw new IllegalArgumentException(
                    eventType.getSimpleName() + " must not be null"
            );
        }

        Set<ConstraintViolation<T>> violations =
                validator.validate(event);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return event;
    }
}