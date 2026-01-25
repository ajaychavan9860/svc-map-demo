package com.demo.microservices.payment.dto;

import com.demo.microservices.payment.model.Payment;

public class PaymentRequest {
    private Long orderId;
    private Payment.PaymentMethod method;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(Long orderId, Payment.PaymentMethod method) {
        this.orderId = orderId;
        this.method = method;
    }
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Payment.PaymentMethod getMethod() { return method; }
    public void setMethod(Payment.PaymentMethod method) { this.method = method; }
}