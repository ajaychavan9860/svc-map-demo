package com.demo.microservices.order.service;

import com.demo.microservices.order.client.ProductServiceClient;
import com.demo.microservices.order.client.UserServiceClient;
import com.demo.microservices.order.dto.OrderRequest;
import com.demo.microservices.order.model.Order;
import com.demo.microservices.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order createOrder(OrderRequest orderRequest) {
        // Validate user exists
        UserServiceClient.UserDto user = userServiceClient.getUserById(orderRequest.getUserId());
        if (user == null) {
            return null;
        }
        
        // Validate product exists and get price
        ProductServiceClient.ProductDto product = productServiceClient.getProductById(orderRequest.getProductId());
        if (product == null) {
            return null;
        }
        
        // Calculate total amount
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity()));
        
        Order order = new Order(orderRequest.getUserId(), orderRequest.getProductId(), 
                               orderRequest.getQuantity(), totalAmount);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }
    
    public String testFeignClients(Long userId, Long productId) {
        StringBuilder result = new StringBuilder();
        
        try {
            // Test User Service Client
            UserServiceClient.UserDto user = userServiceClient.getUserById(userId);
            result.append("[OK] User Service Communication: ");
            if (user != null) {
                result.append("SUCCESS - Found user: ").append(user.getName()).append(" (").append(user.getEmail()).append(")");
            } else {
                result.append("FAILED - User not found");
            }
            result.append("\n");
            
            // Test Product Service Client  
            ProductServiceClient.ProductDto product = productServiceClient.getProductById(productId);
            result.append("[OK] Product Service Communication: ");
            if (product != null) {
                result.append("SUCCESS - Found product: ").append(product.getName()).append(" ($").append(product.getPrice()).append(")");
            } else {
                result.append("FAILED - Product not found");
            }
            result.append("\n");
            
        } catch (Exception e) {
            result.append("[FAIL] ERROR: ").append(e.getMessage());
        }
        
        return result.toString();
    }
}