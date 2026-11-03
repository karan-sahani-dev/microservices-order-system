package com.karan.inventory_service.messaging;

import com.karan.inventory_service.event.InventoryRejectedEvent;
import com.karan.inventory_service.event.InventoryReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryReservedTopic;
    private final String inventoryRejectedTopic;

    public InventoryEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.inventory-reserved}")
            String inventoryReservedTopic,
            @Value("${app.kafka.topics.inventory-rejected}")
            String inventoryRejectedTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.inventoryReservedTopic = inventoryReservedTopic;
        this.inventoryRejectedTopic = inventoryRejectedTopic;
    }

    public CompletableFuture<SendResult<String, Object>> publish(
            InventoryReservedEvent event) {

        return sendEvent(
                inventoryReservedTopic,
                event.getOrderId(),
                event,
                "InventoryReserved"
        );
    }

    public CompletableFuture<SendResult<String, Object>> publish(
            InventoryRejectedEvent event) {

        return sendEvent(
                inventoryRejectedTopic,
                event.getOrderId(),
                event,
                "InventoryRejected"
        );
    }

    private CompletableFuture<SendResult<String, Object>> sendEvent(
            String topic,
            Long orderId,
            Object event,
            String eventName) {

        String key = orderId.toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "{} publish failed: orderId={}",
                        eventName,
                        orderId,
                        exception
                );
            } else {
                log.info(
                        "{} published: orderId={}, topic={}, partition={}, offset={}",
                        eventName,
                        orderId,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });

        // Listener broker acknowledgement ka wait karega
        return future;
    }
}