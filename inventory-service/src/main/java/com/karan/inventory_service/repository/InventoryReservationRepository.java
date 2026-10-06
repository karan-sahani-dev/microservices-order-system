package com.karan.inventory_service.repository;

import com.karan.inventory_service.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    // Same order ID dobara insert hua toh primary key constraint reject karegi
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = """
                    INSERT INTO inventory_reservations
                        (order_id, product_id, quantity, user_id)
                    VALUES
                        (:orderId, :productId, :quantity, :userId)
                    """,
            nativeQuery = true
    )
    int insertReservation(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("quantity") int quantity,
            @Param("userId") Long userId
    );
}