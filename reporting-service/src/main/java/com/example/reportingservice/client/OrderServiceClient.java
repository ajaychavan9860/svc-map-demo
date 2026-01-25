package com.example.reportingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    
    @GetMapping("/api/orders")
    List<Object> getAllOrders();
    
    @GetMapping("/api/orders/user/{userId}")
    List<Object> getOrdersByUserId(@PathVariable("userId") Long userId);
}