package com.demo.microservices.payment.service;

import com.demo.microservices.payment.client.OrderServiceClient;
import com.demo.microservices.payment.dto.PaymentRequest;
import com.demo.microservices.payment.model.Payment;
import com.demo.microservices.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
    
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    public Payment processPayment(PaymentRequest paymentRequest) {
        // Validate order exists
        OrderServiceClient.OrderDto order = orderServiceClient.getOrderById(paymentRequest.getOrderId());
        if (order == null) {
            return null;
        }
        
        Payment payment = new Payment(paymentRequest.getOrderId(), order.getUserId(), 
                                    order.getTotalAmount(), paymentRequest.getMethod());
        
        // Simulate payment processing
        boolean paymentSuccess = simulatePaymentProcessing();
        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId(UUID.randomUUID().toString());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
        }
        
        return paymentRepository.save(payment);
    }
    
    public Payment refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            return paymentRepository.save(payment);
        }
        return null;
    }
    
    private boolean simulatePaymentProcessing() {
        // Simulate 90% success rate
        return Math.random() > 0.1;
    }
}