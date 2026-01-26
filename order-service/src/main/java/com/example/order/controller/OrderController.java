package com.example.order.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> orders = new ArrayList<>();
        orders.add(new OrderResponse(1L, userId, "COMPLETED", 99.99));
        orders.add(new OrderResponse(2L, userId, "PENDING", 149.99));
        return orders;
    }
    
    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable Long orderId) {
        return new OrderResponse(orderId, 1L, "COMPLETED", 99.99);
    }
    
    static class OrderResponse {
        private Long id;
        private Long userId;
        private String status;
        private Double total;
        
        public OrderResponse(Long id, Long userId, String status, Double total) {
            this.id = id;
            this.userId = userId;
            this.status = status;
            this.total = total;
        }
        
        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getStatus() { return status; }
        public Double getTotal() { return total; }
    }
}
