package com.karan.inventory_service.messaging;

import com.karan.inventory_service.entity.InventoryReservation;
import com.karan.inventory_service.event.InventoryReservedEvent;
import com.karan.inventory_service.event.OrderPlacedEvent;
import com.karan.inventory_service.service.InventoryReservationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedListener {

    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final InventoryReservationService reservationService;
    private final InventoryEventPublisher eventPublisher;

    @Value("${app.kafka.publish-timeout-seconds}")
    private long publishTimeoutSeconds;

    @KafkaListener(
            topics = "${app.kafka.topics.order-placed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) throws Exception {

        // Kafka se mili JSON String ko local event object banayenge
        OrderPlacedEvent event =
                jsonMapper.readValue(message, OrderPlacedEvent.class);

        if (event == null) {
            throw new IllegalArgumentException(
                    "OrderPlaced event must not be null");
        }

        // Kafka payload ke validation rules explicitly check karenge
        Set<ConstraintViolation<OrderPlacedEvent>> violations =
                validator.validate(event);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Is service call ke successful return tak DB transaction commit hogi
        InventoryReservation reservation =
                reservationService.reserveForOrder(event);

        InventoryReservedEvent resultEvent = new InventoryReservedEvent(
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getUserId()
        );

        try {
            // Result publish fail hua toh listener successful return nahi karega
            eventPublisher.publish(resultEvent)
                    .get(publishTimeoutSeconds, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        }

        log.info(
                "OrderPlaced processed: orderId={}",
                event.getOrderId());
    }
}