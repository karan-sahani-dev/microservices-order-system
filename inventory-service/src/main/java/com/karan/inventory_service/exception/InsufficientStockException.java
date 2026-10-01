package com.karan.inventory_service.exception;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException(Long productId, int requestedQuantity){
        super("Insufficient stock for product ID:" + productId
        + ", requested quantity:" + requestedQuantity);
    }
}
