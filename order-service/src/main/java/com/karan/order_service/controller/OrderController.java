package com.karan.order_service.controller;


import com.karan.order_service.dto.CreateOrderRequest;
import com.karan.order_service.dto.OrderResponse;
import com.karan.order_service.entity.Order;
import com.karan.order_service.service.OrderService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order savedOrder = orderService.createOrder(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
        );
        return OrderResponse.from(savedOrder);
    }
    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable
            @Positive(message = "Order ID must be greater than zero")
            Long id
    )  {
        Order order = orderService.getOrderById(id);
        return OrderResponse.from(order);
    }
}
