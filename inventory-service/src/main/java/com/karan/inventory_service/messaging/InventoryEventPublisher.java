package com.karan.inventory_service.messaging;

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

    private final KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;
    private final String inventoryReservedTopic;

    public InventoryEventPublisher(
            KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.inventory-reserved}")
            String inventoryReservedTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.inventoryReservedTopic = inventoryReservedTopic;
    }

    public CompletableFuture<SendResult<String, InventoryReservedEvent>> publish(
            InventoryReservedEvent event) {

        // Original order ID ko result event ki key banayenge
        String key = event.getOrderId().toString();

        CompletableFuture<SendResult<String, InventoryReservedEvent>> future =
                kafkaTemplate.send(inventoryReservedTopic, key, event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "InventoryReserved publish failed: orderId={}",
                        event.getOrderId(),
                        exception);
            } else {
                log.info(
                        "InventoryReserved published: orderId={}, topic={}, partition={}, offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        // Listener acknowledgement ka result check karega
        return future;
    }
}