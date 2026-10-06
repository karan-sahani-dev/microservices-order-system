package com.karan.inventory_service.service;

import com.karan.inventory_service.entity.InventoryReservation;
import com.karan.inventory_service.event.OrderPlacedEvent;
import com.karan.inventory_service.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryService inventoryService;

    @Transactional
    public InventoryReservation reserveForOrder(OrderPlacedEvent event) {

        Optional<InventoryReservation> existing =
                reservationRepository.findById(event.getOrderId());

        if (existing.isPresent()) {
            InventoryReservation reservation = existing.get();

            // Same order ID ke saath different data accept nahi karenge
            boolean sameDetails =
                    reservation.getProductId().equals(event.getProductId())
                            && reservation.getQuantity() == event.getQuantity()
                            && reservation.getUserId().equals(event.getUserId());

            if (!sameDetails) {
                throw new IllegalArgumentException(
                        "Conflicting reservation details for order: "
                                + event.getOrderId());
            }

            // Replay par stock dobara reserve nahi hoga
            return reservation;
        }

        // Constructor IDs aur quantity ki basic validation karega
        InventoryReservation reservation = new InventoryReservation(
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getUserId()
        );

        // Primary key concurrent duplicate inserts ko reject karegi
        reservationRepository.insertReservation(
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getUserId()
        );

        // Stock update isi MySQL transaction ka part rahega
        inventoryService.reserveStock(
                reservation.getProductId(),
                reservation.getQuantity()
        );

        return reservation;
    }
}