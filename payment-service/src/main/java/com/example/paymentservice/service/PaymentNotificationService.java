package com.example.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class PaymentNotificationService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendPaymentConfirmationEmail(Long paymentId, String customerEmail, Double amount) {
        try {
            // Call email-service to send payment confirmation
            String emailServiceUrl = "http://email-service:8087/api/email/send";
            
            PaymentEmailRequest emailRequest = new PaymentEmailRequest();
            emailRequest.setTo(customerEmail);
            emailRequest.setSubject("Payment Confirmation - Payment #" + paymentId);
            emailRequest.setMessage("Your payment of $" + amount + " has been successfully processed. Payment ID: " + paymentId);
            
            restTemplate.postForObject(emailServiceUrl, emailRequest, String.class);
            System.out.println("Payment confirmation email sent for payment: " + paymentId);
            
        } catch (Exception e) {
            System.err.println("Failed to send payment email: " + e.getMessage());
        }
    }

    public void logPaymentEvent(Long paymentId, String event, Double amount) {
        try {
            // Send payment event to logging-service via Kafka
            String logMessage = String.format("PAYMENT_EVENT: Payment ID=%d, Event=%s, Amount=$%.2f", 
                paymentId, event, amount);
            
            kafkaTemplate.send("microservices-logs", logMessage);
            System.out.println("Payment event logged: " + event);
            
        } catch (Exception e) {
            System.err.println("Failed to log payment event: " + e.getMessage());
        }
    }
    
    // Inner class for email request
    public static class PaymentEmailRequest {
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