package com.karan.notification_service.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Stock reserve hone ke baad aane wale Kafka event ka data model
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservedEvent {

    @NotNull
    @Positive
    private Long orderId;

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private Long userId;
}