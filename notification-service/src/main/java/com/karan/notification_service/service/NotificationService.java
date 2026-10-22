package com.karan.notification_service.service;
import com.karan.notification_service.event.InventoryReservedEvent;
import com.karan.notification_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public void notifyOrderReceived(OrderPlacedEvent orderPlacedEvent) {
        log.info(
                "Order Received | userId={} | orderId={} | productId={} | quantity={}",
                orderPlacedEvent.getUserId(),
                orderPlacedEvent.getOrderId(),
                orderPlacedEvent.getProductId(),
                orderPlacedEvent.getQuantity()
        );
    }
    public void notifyStockReserved(InventoryReservedEvent inventoryReservedEvent) {
        log.info(
                "Stock reserved | userId={} | orderId={} | productId={} | quantity={}",
                inventoryReservedEvent.getUserId(),
                inventoryReservedEvent.getOrderId(),
                inventoryReservedEvent.getProductId(),
                inventoryReservedEvent.getQuantity()
        );
    }
}
