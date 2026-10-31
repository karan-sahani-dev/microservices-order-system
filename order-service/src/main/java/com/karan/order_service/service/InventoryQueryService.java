package com.karan.order_service.service;

import com.karan.order_service.client.InventoryClient;
import com.karan.order_service.dto.InventoryStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {
    private final InventoryClient inventoryClient;
    public InventoryStockResponse getInventory(Long productId){
        return inventoryClient.getInventory(productId);
    }
}
