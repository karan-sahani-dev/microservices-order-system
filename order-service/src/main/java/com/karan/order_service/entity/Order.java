package com.karan.order_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Order(Long userId, Long productId, int quantity) {
        if (userId == null || userId <= 0
                || productId == null || productId <= 0
                || quantity <= 0) {

            throw new IllegalArgumentException(
                    "Order IDs and quantity must be positive"
            );
        }

        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus targetStatus) {

        // Same outcome ka replay harmless hai
        if (status == targetStatus) {
            return;
        }

        // Final outcome ko opposite outcome se overwrite nahi karenge
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot change order " + id
                            + " from " + status
                            + " to " + targetStatus
            );
        }

        this.status = targetStatus;
    }
}