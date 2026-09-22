package com.karan.order_service.dto;

import com.karan.order_service.entity.Order;
import com.karan.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class OrderResponse {
     private final Long id;
     private final Long userId;
     private final Long productId;
     private final int quantity;
     private final OrderStatus status;
     private final Instant createdAt;

     public static OrderResponse from(Order order){
         return new OrderResponse(
                 order.getId(),
                 order.getUserId(),
                 order.getProductId(),
                 order.getQuantity(),
                 order.getStatus(),
                 order.getCreatedAt()
         );
     }
}
