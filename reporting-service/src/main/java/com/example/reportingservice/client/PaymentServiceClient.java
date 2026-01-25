package com.example.reportingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "payment-service") 
public interface PaymentServiceClient {
    
    @GetMapping("/api/payments")
    List<Object> getAllPayments();
    
    @GetMapping("/api/payments/order/{orderId}")
    Object getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}