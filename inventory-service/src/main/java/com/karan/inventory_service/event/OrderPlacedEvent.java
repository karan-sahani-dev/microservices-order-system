package com.karan.inventory_service.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderPlacedEvent {

    // Reservation ko original order se identify karenge
    @NotNull
    @Positive
    private Long orderId;

    // Is product ka stock reserve karna hai
    @NotNull
    @Positive
    private Long productId;

    // Reserve hone wali units positive honi chahiye
    @NotNull
    @Positive
    private Integer quantity;

    // Order place karne wala user
    @NotNull
    @Positive
    private Long userId;
}