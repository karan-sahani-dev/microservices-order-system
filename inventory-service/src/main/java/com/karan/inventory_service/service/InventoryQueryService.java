package com.karan.inventory_service.service;

import com.karan.inventory_service.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {
    private final InventoryService inventoryService;

    @Cacheable(
            cacheNames = "inventory-stock",
            key = "#p0",
            condition = "#p0 != null && #p0 > 0"
    )
       public InventoryResponse getInventory(Long productId){
        return InventoryResponse.from(inventoryService.getInventory(productId));
    }
}
