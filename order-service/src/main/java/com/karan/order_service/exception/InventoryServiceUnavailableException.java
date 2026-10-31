package com.karan.order_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class InventoryServiceUnavailableException extends  RuntimeException {
    public InventoryServiceUnavailableException(String message, Throwable cause){
        super(message,cause);
    }
}
