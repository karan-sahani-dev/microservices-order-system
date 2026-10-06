package com.karan.inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation {

    // Ek order ki sirf ek reservation: primary key duplicates rokegi
    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    public InventoryReservation(
            Long orderId,
            Long productId,
            int quantity,
            Long userId) {

        if (orderId == null || orderId <= 0
                || productId == null || productId <= 0
                || userId == null || userId <= 0
                || quantity <= 0) {

            throw new IllegalArgumentException(
                    "Reservation IDs and quantity must be positive");
        }

        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
    }
}