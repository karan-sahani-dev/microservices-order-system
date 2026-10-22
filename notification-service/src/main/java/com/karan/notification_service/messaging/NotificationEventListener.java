package com.karan.notification_service.messaging;
import com.karan.notification_service.event.InventoryReservedEvent;
import com.karan.notification_service.event.OrderPlacedEvent;
import com.karan.notification_service.service.NotificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topics.order-placed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onOrderPlaced(String message) throws Exception {
        OrderPlacedEvent event = jsonMapper.readValue(message, OrderPlacedEvent.class);
          validator(event);
        notificationService.notifyOrderReceived(event);
    }
    @KafkaListener(
            topics = "${app.kafka.topics.inventory-reserved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onInventoryReserved(String message) throws Exception {
        InventoryReservedEvent event = jsonMapper.readValue(message, InventoryReservedEvent.class);
        validator(event);
        notificationService.notifyStockReserved(event);
    }
    private <T> void validator(T event){
        if(event == null){
            throw new IllegalArgumentException("Event must not be null");
        }
        Set<ConstraintViolation<T>> voilations = validator.validate(event);
        if(!voilations.isEmpty()){
            throw new ConstraintViolationException(voilations);
        }
    }
}
