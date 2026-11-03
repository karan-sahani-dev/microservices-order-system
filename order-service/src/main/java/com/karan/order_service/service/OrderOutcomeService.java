package com.karan.order_service.service;

import com.karan.order_service.entity.Order;
import com.karan.order_service.event.InventoryRejectedEvent;
import com.karan.order_service.event.InventoryReservedEvent;
import com.karan.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderOutcomeService {

    private final OrderRepository orderRepository;

    @Transactional
    public void confirmOrder(InventoryReservedEvent event) {

        Order order = findMatchingOrder(
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getUserId()
        );

        order.confirm();
    }

    @Transactional
    public void cancelOrder(InventoryRejectedEvent event) {

        Order order = findMatchingOrder(
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getUserId()
        );

        order.cancel();
    }

    private Order findMatchingOrder(
            Long orderId,
            Long productId,
            Integer quantity,
            Long userId) {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Order not available yet: " + orderId
                ));

        boolean sameDetails =
                Objects.equals(order.getProductId(), productId)
                        && Objects.equals(order.getQuantity(), quantity)
                        && Objects.equals(order.getUserId(), userId);

        if (!sameDetails) {
            throw new IllegalArgumentException(
                    "Inventory outcome conflicts with order details: "
                            + orderId
            );
        }

        return order;
    }
}