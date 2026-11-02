package com.karan.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.karan.inventory_service.entity.Inventory;
import lombok.Getter;

@Getter
public class InventoryResponse {

    private final Long productId;
    private final int quantity;
    private final int reservedQuantity;
    private final int availableQuantity;

    // Redis ke JSON se immutable DTO banane ke liye constructor mapping.
    @JsonCreator
    public InventoryResponse(
            @JsonProperty("productId") Long productId,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("reservedQuantity") int reservedQuantity,
            @JsonProperty("availableQuantity") int availableQuantity) {

        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}