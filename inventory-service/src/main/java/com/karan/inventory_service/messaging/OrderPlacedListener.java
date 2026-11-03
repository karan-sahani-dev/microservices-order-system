package com.karan.inventory_service.messaging;

import com.karan.inventory_service.entity.InventoryReservation;
import com.karan.inventory_service.event.InventoryRejectedEvent;
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
import java.util.concurrent.CompletableFuture;
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

        OrderPlacedEvent event =
                jsonMapper.readValue(message, OrderPlacedEvent.class);

        if (event == null) {
            throw new IllegalArgumentException(
                    "OrderPlaced event must not be null"
            );
        }

        Set<ConstraintViolation<OrderPlacedEvent>> violations =
                validator.validate(event);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Service return hone se pehle MySQL transaction commit hogi
        InventoryReservation reservation =
                reservationService.reserveForOrder(event);

        CompletableFuture<?> publishFuture =
                switch (reservation.getStatus()) {

                    case RESERVED -> eventPublisher.publish(
                            new InventoryReservedEvent(
                                    reservation.getOrderId(),
                                    reservation.getProductId(),
                                    reservation.getQuantity(),
                                    reservation.getUserId()
                            )
                    );

                    case REJECTED -> eventPublisher.publish(
                            new InventoryRejectedEvent(
                                    reservation.getOrderId(),
                                    reservation.getProductId(),
                                    reservation.getQuantity(),
                                    reservation.getUserId(),
                                    reservation.getRejectionReason()
                            )
                    );

                    case PENDING -> throw new IllegalStateException(
                            "Cannot publish a pending reservation: "
                                    + reservation.getOrderId()
                    );
                };

        try {
            publishFuture.get(
                    publishTimeoutSeconds,
                    TimeUnit.SECONDS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        }

        log.info(
                "OrderPlaced processed: orderId={}, status={}",
                reservation.getOrderId(),
                reservation.getStatus()
        );
    }
}