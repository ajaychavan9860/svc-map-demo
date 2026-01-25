package com.example.reportingservice.service;

import com.example.reportingservice.client.OrderServiceClient;
import com.example.reportingservice.client.PaymentServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    @Autowired
    private WebClient.Builder webClientBuilder;

    public Map<String, Object> generateDashboardReport() {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Fetch data from multiple services using Feign clients
            List<Object> orders = orderServiceClient.getAllOrders();
            List<Object> payments = paymentServiceClient.getAllPayments();
            
            report.put("totalOrders", orders.size());
            report.put("totalPayments", payments.size());
            
            // Also call user service using WebClient
            String userServiceResponse = webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users/count")
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            report.put("userServiceResponse", userServiceResponse);
            
        } catch (Exception e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            report.put("error", "Failed to fetch data from dependent services");
        }
        
        return report;
    }
}