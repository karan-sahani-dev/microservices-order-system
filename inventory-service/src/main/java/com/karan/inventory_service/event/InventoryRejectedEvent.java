package com.karan.inventory_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryRejectedEvent {
    private final Long orderId;
    private final long productId;
    private final Integer quantity;
    private final Long userId;
    private final  String reason;
}
