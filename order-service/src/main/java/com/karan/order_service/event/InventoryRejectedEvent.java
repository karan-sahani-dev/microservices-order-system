package com.karan.order_service.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryRejectedEvent {

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

    @NotNull
    @Pattern(regexp = "PRODUCT_NOT_FOUND|INSUFFICIENT_STOCK")
    private String reason;
}