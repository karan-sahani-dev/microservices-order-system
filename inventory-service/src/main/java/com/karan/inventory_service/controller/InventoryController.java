package com.karan.inventory_service.controller;

import com.karan.inventory_service.dto.AddStockRequest;
import com.karan.inventory_service.dto.InventoryResponse;
import com.karan.inventory_service.dto.ReserveStockRequest;
import com.karan.inventory_service.entity.Inventory;
import com.karan.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    public InventoryResponse addStock(@Valid @RequestBody AddStockRequest request) {
        Inventory inventory = inventoryService.addStock(
                request.getProductId(),
                request.getQuantity()
        );
        return InventoryResponse.from(inventory);
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable("productId") Long productId) {
        Inventory inventory = inventoryService.getInventory(productId);
        return InventoryResponse.from(inventory);
    }

    @PostMapping("/reserve")
    public InventoryResponse reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        Inventory inventory = inventoryService.reserveStock(
                request.getProductId(),
                request.getQuantity()
        );
        return InventoryResponse.from(inventory);
    }
}
