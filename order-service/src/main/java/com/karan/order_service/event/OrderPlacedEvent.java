package com.karan.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderPlacedEvent {
    private final Long orderId;
    private final Long productId;
    private final Integer quantity;
    private final Long userId;
}
