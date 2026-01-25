package com.example.analyticsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BusinessAnalyticsService {

    @Autowired
    private RestTemplate restTemplate;

    public String generateUserAnalytics() {
        try {
            // Connect to user-service for user statistics
            String userServiceUrl = "http://user-service:8081/api/users/analytics";
            String userStats = restTemplate.getForObject(userServiceUrl, String.class);
            
            // Log analytics generation event
            String loggingServiceUrl = "http://logging-service:8088/api/logs/analytics";
            restTemplate.postForObject(loggingServiceUrl, 
                "Analytics generated for users", String.class);
            
            return "User Analytics: " + userStats;
        } catch (Exception e) {
            return "Error generating user analytics: " + e.getMessage();
        }
    }

    public String generateOrderAnalytics() {
        try {
            // Connect to order-service for order statistics
            String orderServiceUrl = "http://order-service:8083/api/orders/analytics";
            String orderStats = restTemplate.getForObject(orderServiceUrl, String.class);
            
            return "Order Analytics: " + orderStats;
        } catch (Exception e) {
            return "Error generating order analytics: " + e.getMessage();
        }
    }

    public String generatePaymentAnalytics() {
        try {
            // Connect to payment-service for payment statistics
            String paymentServiceUrl = "http://payment-service:8084/api/payments/analytics";
            String paymentStats = restTemplate.getForObject(paymentServiceUrl, String.class);
            
            return "Payment Analytics: " + paymentStats;
        } catch (Exception e) {
            return "Error generating payment analytics: " + e.getMessage();
        }
    }

    public String generateInventoryAnalytics() {
        try {
            // Connect to inventory-service for inventory statistics
            String inventoryServiceUrl = "http://inventory-service:8085/api/inventory/analytics";
            String inventoryStats = restTemplate.getForObject(inventoryServiceUrl, String.class);
            
            return "Inventory Analytics: " + inventoryStats;
        } catch (Exception e) {
            return "Error generating inventory analytics: " + e.getMessage();
        }
    }
}