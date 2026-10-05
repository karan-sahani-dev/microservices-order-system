package com.karan.order_service.service;

import com.karan.order_service.entity.Order;
import com.karan.order_service.event.OrderPlacedEvent;
import com.karan.order_service.messaging.OrderEventPublisher;
import com.karan.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Value("${app.kafka.publish-timeout-seconds}")
    private long publishTimeoutSeconds;

    @Transactional
    public Order createOrder(Long userId, Long productId, int quantity) {
        Order order = new Order(userId, productId, quantity);


        Order savedOrder = orderRepository.save(order);


        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getUserId()
        );

        try {

            orderEventPublisher.publish(event)
                    .get(publishTimeoutSeconds, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing OrderPlaced event",
                    exception);

        } catch (ExecutionException exception) {

            throw new IllegalStateException(
                    "Failed to publish OrderPlaced event",
                    exception.getCause());

        } catch (TimeoutException exception) {

            throw new IllegalStateException(
                    "Timed out waiting for OrderPlaced acknowledgement",
                    exception);
        }

        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found with id: " + id));
    }
}