package com.karan.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringBootVersion;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id" , nullable = false , unique = true)
    private  Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    public Inventory(Long productId, int quantity){
        if(productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if(quantity < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = 0;
    }
    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
