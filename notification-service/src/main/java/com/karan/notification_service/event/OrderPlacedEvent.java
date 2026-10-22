package com.karan.notification_service.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// OrderPlaced JSON receive karne ka data model; database entity nahi hai
@Getter
@Setter
@NoArgsConstructor
public class OrderPlacedEvent {

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