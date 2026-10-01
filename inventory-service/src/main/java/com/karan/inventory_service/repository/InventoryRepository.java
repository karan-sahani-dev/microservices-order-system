package com.karan.inventory_service.repository;

import com.karan.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface InventoryRepository  extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    @Modifying(flushAutomatically = true , clearAutomatically = true)
    @Query(
            value = """
                    INSERT INTO inventory (product_id, quantity, reserved_quantity)
                    VALUES (:productId, :quantity, 0)
                    ON DUPLICATE KEY UPDATE quantity = quantity + :quantity
                    """ , nativeQuery = true)
       void addStock(
               @Param("productId") Long productId,
               @Param("quantity") int quantity
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Inventory i
            SET i.reservedQuantity = i.reservedQuantity + :quantity
            WHERE i.productId = :productId
            AND :quantity > 0
            AND (i.quantity - i.reservedQuantity) >= :quantity
            """)

    int reserveStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );
}
