package com.karan.inventory_service.service;

import com.karan.inventory_service.exception.InventoryNotFoundException;
import com.karan.inventory_service.entity.Inventory;
import com.karan.inventory_service.exception.InsufficientStockException;
import com.karan.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public Inventory getInventory(Long productId) {
        validateProductId(productId);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(
                        () -> new InventoryNotFoundException(productId)
                );
    }
    @Transactional
    public Inventory addStock(Long productId, int quantity) {
        validateProductId(productId);
        validateQuantity(quantity);

        inventoryRepository.addStock(productId, quantity);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(
                        () -> new InventoryNotFoundException(productId)
                );
    }
    @Transactional
    public Inventory reserveStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }
        int updatedRows = inventoryRepository.reserveStock(productId, quantity);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(
                        () -> new InventoryNotFoundException(productId)
            );
        if (updatedRows == 0) {
            throw new InsufficientStockException(productId, quantity);
        }
        return inventory;
    }
    private void validateProductId(Long productId){
        if(productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be positive"
            );
        }
    }

    private void validateQuantity(int quantity) {
        if(quantity <= 0){
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }
}
