package com.karan.inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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


    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "rejection_reason", length = 64)
    private String rejectionReason;

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
        this.status = ReservationStatus.PENDING;
    }

    public void markReserved() {
        requirePending();

        this.status = ReservationStatus.RESERVED;
        this.rejectionReason = null;
    }

    public void markRejected(String reason) {
        requirePending();

        if (reason == null || reason.isBlank() || reason.length() > 64) {
            throw new IllegalArgumentException(
                    "Rejection reason must contain 1 to 64 characters");
        }

        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    private void requirePending() {

        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Reservation outcome is already final for order: " + orderId);
        }
    }
}