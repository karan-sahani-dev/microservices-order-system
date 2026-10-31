package com.karan.order_service.client;

import com.karan.order_service.dto.InventoryStockResponse;
import com.karan.order_service.exception.InventoryServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class InventoryClient {

    private final WebClient inventoryWebClient;
    private final Duration requestTimeout;

    public InventoryClient(
            @Qualifier("inventoryWebClient") WebClient inventoryWebClient,
            @Value("${app.inventory.request-timeout-seconds}") long timeoutSeconds) {

        this.inventoryWebClient = inventoryWebClient;
        this.requestTimeout = Duration.ofSeconds(timeoutSeconds);
    }

    public InventoryStockResponse getInventory(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID must be positive"
            );
        }


        try {
            InventoryStockResponse response = inventoryWebClient
                    .get()
                    .uri("/api/inventory/{productId}", productId)
                    .retrieve()
                    .bodyToMono(InventoryStockResponse.class)
                    .timeout(requestTimeout)
                    .onErrorMap(
                            TimeoutException.class,
                            error -> new InventoryServiceUnavailableException(
                                    "Inventory service response timed out", error)
                    )
                    .block();

            // Empty response ko valid stock information nahi maan sakte.
            if (response == null) {
                throw new InventoryServiceUnavailableException(
                        "Inventory service returned an empty response",
                        new IllegalStateException("Inventory response body was empty")
                );
            }

            return response;

        } catch (WebClientResponseException exception) {
            // Product missing aur service unavailable alag situations hain.
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for product ID: " + productId,
                        exception
                );
            }

            throw new InventoryServiceUnavailableException(
                    "Inventory service returned an unsuccessful response",
                    exception
            );

        } catch (WebClientRequestException exception) {
            // Connection failure jaise network errors ko handle karta hai.
            throw new InventoryServiceUnavailableException(
                    "Unable to connect to inventory service",
                    exception
            );
        }
    }
}