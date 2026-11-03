package com.karan.inventory_service.service;

import com.karan.inventory_service.entity.InventoryReservation;
import com.karan.inventory_service.entity.ReservationStatus;
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

            boolean sameDetails =
                    reservation.getProductId().equals(event.getProductId())
                            && reservation.getQuantity() == event.getQuantity()
                            && reservation.getUserId().equals(event.getUserId());

            if (!sameDetails) {
                throw new IllegalArgumentException(
                        "Conflicting reservation details for order: "
                                + event.getOrderId()
                );
            }

            if (reservation.getStatus() == ReservationStatus.PENDING) {
                throw new IllegalStateException(
                        "Reservation outcome is not final for order: "
                                + event.getOrderId()
                );
            }

            // Replay par pehle se saved outcome return hoga
            return reservation;
        }

        InventoryReservation newReservation = new InventoryReservation(
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getUserId()
        );

        reservationRepository.insertReservation(
                newReservation.getOrderId(),
                newReservation.getProductId(),
                newReservation.getQuantity(),
                newReservation.getUserId()
        );

        StockReservationResult result = inventoryService.tryReserveStock(
                newReservation.getProductId(),
                newReservation.getQuantity()
        );

        // Repository updates persistence context clear karte hain,
        // isliye outcome update karne se pehle entity reload karenge
        InventoryReservation reservation =
                reservationRepository.findById(event.getOrderId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Reservation disappeared for order: "
                                        + event.getOrderId()
                        ));

        switch (result) {
            case RESERVED -> reservation.markReserved();

            case PRODUCT_NOT_FOUND ->
                    reservation.markRejected("PRODUCT_NOT_FOUND");

            case INSUFFICIENT_STOCK ->
                    reservation.markRejected("INSUFFICIENT_STOCK");
        }

        // Managed entity ke changes transaction commit par save honge
        return reservation;
    }
}