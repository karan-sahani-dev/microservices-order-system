package com.karan.order_service.service;

import com.karan.order_service.entity.Order;
import com.karan.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    @Transactional
    public Order createOrder(Long userId, Long productId, int quantity) {
        Order order = new Order(userId, productId, quantity);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id){
        return orderRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Order not found with id:" + id));
    }
}