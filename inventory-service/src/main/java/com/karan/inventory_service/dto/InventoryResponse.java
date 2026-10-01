package com.karan.inventory_service.dto;

import com.karan.inventory_service.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryResponse {
    private final Long productId;
    private final int quantity;
    private final int reservedQuantity;
    private final int availableQuantity;

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}
