package com.karan.inventory_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryReservedEvent {

    // Kis order ke liye reservation successful hui
    private final Long orderId;

    // Kis product ka stock reserve hua
    private final Long productId;

    // Kitni units reserve hui
    private final Integer quantity;

    // Order kis user ka hai
    private final Long userId;
}