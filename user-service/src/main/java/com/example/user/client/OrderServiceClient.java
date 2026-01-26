package com.example.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * Feign client to call Order Service
 * user-service -> order-service
 */
@FeignClient(name = "${feign.order.name}", url = "${feign.order.url}")
public interface OrderServiceClient {
    
    @GetMapping("/api/orders/user/{userId}")
    List<OrderDto> getOrdersByUserId(@PathVariable Long userId);
    
    @GetMapping("/api/orders/{orderId}")
    OrderDto getOrderById(@PathVariable Long orderId);
}

class OrderDto {
    private Long id;
    private Long userId;
    private String status;
    private Double total;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
