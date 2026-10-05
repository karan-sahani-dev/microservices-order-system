package com.karan.order_service.messaging;

import com.karan.order_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String orderPlacedTopic;

    public OrderEventPublisher(
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-placed}") String orderPlacedTopic) {


        this.kafkaTemplate = kafkaTemplate;
        this.orderPlacedTopic = orderPlacedTopic;
    }
      public CompletableFuture<SendResult<String, OrderPlacedEvent>> publish(OrderPlacedEvent event) {
       String key  = event.getOrderId().toString();

       CompletableFuture<SendResult<String, OrderPlacedEvent>> future = kafkaTemplate.send(orderPlacedTopic,key,event);

       future.whenComplete((result, exception) -> {
           if (exception != null) {
               log.error("Order publish failed: orderId={}",event.getOrderId(),exception);
           }  else{
               log.info("Order published: orderId={}, topic={}, partition={}, offset={}",
                       event.getOrderId(),
                       result.getRecordMetadata().topic(),
                       result.getRecordMetadata().partition(),
                       result.getRecordMetadata().offset());
           }
       });
       return future;
      }
}
