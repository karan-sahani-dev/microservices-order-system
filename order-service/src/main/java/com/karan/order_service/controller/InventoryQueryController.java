package com.karan.order_service.controller;

import com.karan.order_service.dto.InventoryStockResponse;
import com.karan.order_service.service.InventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/inventory")
public class InventoryQueryController {

    private final InventoryQueryService inventoryQueryService;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryStockResponse> getInventory(
            @PathVariable("productId") Long productId) {

        // Stock information fetch karke HTTP 200 response return karta hai.
        InventoryStockResponse response =
                inventoryQueryService.getInventory(productId);

        return ResponseEntity.ok(response);
    }
}