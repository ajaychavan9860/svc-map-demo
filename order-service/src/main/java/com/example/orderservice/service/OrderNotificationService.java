package com.example.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderNotificationService {

    @Autowired
    private RestTemplate restTemplate;

    public void sendOrderConfirmationEmail(Long orderId, String customerEmail) {
        try {
            // Call email-service to send order confirmation
            String emailServiceUrl = "http://email-service:8087/api/email/send";
            
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(customerEmail);
            emailRequest.setSubject("Order Confirmation - Order #" + orderId);
            emailRequest.setMessage("Your order #" + orderId + " has been confirmed and is being processed.");
            
            restTemplate.postForObject(emailServiceUrl, emailRequest, String.class);
            System.out.println("Order confirmation email sent for order: " + orderId);
            
        } catch (Exception e) {
            System.err.println("Failed to send email for order: " + orderId + " - " + e.getMessage());
        }
    }

    public void sendOrderStatusUpdate(Long orderId, String status, String customerEmail) {
        try {
            // Call email-service for status updates
            String emailServiceUrl = "http://email-service:8087/api/email/send";
            
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(customerEmail);
            emailRequest.setSubject("Order Status Update - Order #" + orderId);
            emailRequest.setMessage("Your order #" + orderId + " status has been updated to: " + status);
            
            restTemplate.postForObject(emailServiceUrl, emailRequest, String.class);
            
        } catch (Exception e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
        }
    }
    
    // Inner class for email request
    public static class EmailRequest {
        private String to;
        private String subject;
        private String message;
        
        // Getters and setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}